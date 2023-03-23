package org.example;

import org.powbot.api.Condition;
import org.powbot.api.rt4.*;
import org.powbot.api.script.*;
import org.powbot.api.script.paint.Paint;
import org.powbot.api.script.paint.PaintBuilder;
import org.powbot.api.script.paint.TrackInventoryOption;
import org.powbot.mobile.script.ScriptManager;

import java.util.List;

import static org.powbot.dax.engine.WaitFor.random;


@ScriptConfiguration(description = "Which material would you like to tan?", name = "Material", allowedValues = {"Green dragonhide", "Blue dragonhide", "Red dragonhide", "Black dragonhide"})
@ScriptConfiguration(description = "Use stamina potions?", name = "Stamina", optionType = OptionType.BOOLEAN)
@ScriptManifest(name= "M5GuildTanner", description="Tans leather in the Crafting Guild. Uses the bank chest (99 Crafting required!).",
                version = "1.0", category = ScriptCategory.MoneyMaking, author = "M5")
public class Main extends AbstractScript {

    public static void main(String[] args) {
        new Main().startScript();
    }

    Material material;
    String hide, leather, status;
    int comp;
    boolean useStamina;


    @Override
    public void onStart(){
       processConfigs();
       buildPaint();
       adjustCamera();
    }

    @Override
    public void poll() {

        System.out.println("Aasa");

      if (Inventory.stream().name(hide).list().size() != 27 && getFloor() == 1){

          status = "Taking hide from bank";
          if (!Bank.opened()) {
              GameObject chest = Objects.stream().name("Bank chest").nearest().first();
              chest.click();
              Condition.wait(Bank::opened, 150, 15);
          }

          // Deposit everything that is not Coins or Hide
          List<Item> inv = Inventory.stream().list();
          for (Item item : inv) {
              if (!item.name().equals("Coins") && !item.name().equals(hide)) {
                  Bank.deposit(item.name(), Bank.Amount.ALL);
              }
          }

          if (Bank.opened() && Bank.stream().name(hide).list().size() == 0)  {
              Game.logout();
              ScriptManager.INSTANCE.stop();

          }

          Bank.withdraw(hide, Bank.Amount.ALL);
          Bank.close();
          Condition.wait( () -> !Bank.opened(), 150, 25);
          if (!Movement.running() && useStamina) Widgets.widget(160).component(29).click();
          if (!Movement.running() && !useStamina && Movement.energyLevel()>50) Widgets.widget(160).component(29).click();

      }

      if (Inventory.stream().name(hide).list().size() > 0 && getFloor() == 1){
           status = "Going to floor 2";
           if(Bank.opened()) Bank.close();
           GameObject stair = Objects.stream().name("Staircase").nearest().first();
           stair.interact("Climb-up");
           Condition.wait( () -> getFloor() == 2,150,25);
           Condition.sleep(random(200,500));
      }

      if (getFloor() == 2 && Inventory.stream().name(hide).list().size() > 0){
          status = "Interacting with tanner";
          if(!Game.tab().name().equals("NONE")) Game.closeOpenTab();
          Npc tanner = Npcs.stream().name("Tanner").first();
          tanner.interact("Trade");
          Condition.wait( () -> Widgets.widget(324).component(comp).visible() ,150,20);
      }

       if (Widgets.widget(324).component(comp).visible() && Inventory.stream().name(hide).list().size() > 0) {
           status = "Tanning menu open. Tanning all leather";
           Widgets.widget(324).component(comp).interact("Tan All");
           Condition.wait( () -> Inventory.stream().name(hide).list().size() == 0,150,25);
       }

       if (getFloor() == 2 && Inventory.stream().name(hide).list().size() == 0){
           status = "Going to floor 1";
           GameObject stair = Objects.stream().name("Staircase").nearest().first();
           stair.interact("Climb-down");
           Condition.wait( () -> getFloor() == 1,150,25);
       }

       if (getFloor() == 1 && Inventory.stream().name(leather).list().size() > 0){
           status = "Depositing leather";
           if(!Bank.opened()) {
               GameObject chest = Objects.stream().name("Bank chest").nearest().first();
               chest.click();
               Condition.wait(Bank::opened,150,15);
           }
           Bank.deposit(leather, Bank.Amount.ALL);


           if (Movement.energyLevel() < 10 && useStamina){
               status = "Drinking stamina potion";
               String potionNumber;
               if(Bank.stream().name("Stamina potion(4)").list().size()>0) potionNumber = "4";
               else if (Bank.stream().name("Stamina potion(3)").list().size()>0) potionNumber = "3";
               else if (Bank.stream().name("Stamina potion(2)").list().size()>0) potionNumber = "2";
               else if (Bank.stream().name("Stamina potion(1)").list().size()>0) potionNumber = "1";
               else {
                   potionNumber = "0";
               }

               if (!potionNumber.equals("0")) {
                   Bank.withdraw("Stamina potion("+potionNumber+")", Bank.Amount.ONE);
                   Condition.wait(() -> Inventory.stream().name("Stamina potion(" + potionNumber + ")").list().size() > 0, 150, 15);
                   Inventory.stream().name("Stamina potion("+potionNumber+")").first().interact("Drink");
               }

           }

       }

    }

    public void processConfigs(){

        if (getOption("Material").equals("Green dragonhide")) material = Material.Green;
        if (getOption("Material").equals("Blue dragonhide"))  material = Material.Blue;
        if (getOption("Material").equals("Red dragonhide"))   material = Material.Red;
        if (getOption("Material").equals("Black dragonhide")) material = Material.Black;
        hide = material.hide;
        leather = material.leather;
        comp = material.comp;

        if (getOption("Stamina")) useStamina = true;

    }

    public void buildPaint(){
        Paint paint = PaintBuilder.newBuilder()
                .x(40)
                .y(45)
                .trackInventoryItem(1745, "Tanned hide: ", TrackInventoryOption.QuantityChange)
                .addString("Status: ", () -> status)
                .build();
        addPaint(paint);

    }

    public void adjustCamera(){
        Camera.moveZoomSlider(0.65);
        Condition.sleep(random(1000,2000));
        Widgets.widget(601).component(41).interact("Look West");
        Condition.sleep(random(1000,2000));
    }

    public int getFloor(){
        if (Npcs.stream().name("Tanner").list().size() == 0) return 1;
        else return 2;
    }

}
