package dev.namesareapain.neidatadump;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.ICraftingHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import codechicken.nei.PositionedStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import java.awt.image.BufferedImage;
import java.util.Optional;
import net.minecraft.util.IIcon;
import javax.imageio.ImageIO;
import java.util.Base64;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import java.io.ByteArrayOutputStream;
import dev.namesareapain.neidatadump.MasterItemList;
import dev.namesareapain.neidatadump.Jsonify;
import dev.namesareapain.neidatadump.handlerhandler.*;
import thaumcraft.api.ThaumcraftApiHelper;
import net.minecraftforge.oredict.OreDictionary;

import static codechicken.nei.recipe.GuiCraftingRecipe.craftinghandlers;

public class ExportData {
    
    public static long ITEM_COUNT = 0;

    public static ArrayList<IHandlerHandler> handlerHandlers(){ 
        ArrayList<IHandlerHandler> out = new ArrayList<IHandlerHandler>();
        out.add(new GT_NEI_DefaultHandlerHandler());
        out.add(new TemplateRecipeHandlerHandler());
        return out;
    }

    public static void exportRecipes(){
        System.out.println("beginning data dump");
        ArrayList<ICraftingHandler> neihandlers = craftinghandlers;
        JSONArray handlerdumps = new JSONArray();
        for(ICraftingHandler handler : neihandlers){
            for(IHandlerHandler handlerHandler : handlerHandlers()){
                if(handlerHandler.claim(handler)){
                    try{
                        handlerdumps.put(handlerHandler.dumpRecipes(handler));
                    } catch (Exception e){
                        System.out.println("Failed to process Handler:" +
                                handler.getRecipeName()
                                );
                    }
                    break;
                }
            }
        }
        JSONObject out = new JSONObject();
        out.put("h",handlerdumps);
        Jsonify.writeOutput(out,"./recipes.json");
    }

    public static void exportItems(){
        JSONArray out = new JSONArray();
        long i = 0;
        for(ItemStack stack : new MasterItemList()){    
            try {
                out.put(new JSONObject()
                    .put("item",Jsonify.item(stack))
                    .put("name",stack.getDisplayName())
                );
                i++;
            } catch(Exception e){
                System.out.println(e.toString());
            }
        }
        Jsonify.writeOutput(new JSONObject().put("items",out),"./itemlist.json");
        ITEM_COUNT = i;
        System.out.println("Total items: " + ITEM_COUNT);
    }

    public static void exportOreDict(){
        JSONArray orenames = new JSONArray();
        for(String orename : OreDictionary.getOreNames()){ 
            JSONArray entries = new JSONArray();
            for(ItemStack entry : OreDictionary.getOres(orename)){
                entries.put(Jsonify.item(entry.getItem()));
            }
            orenames.put(new JSONObject()
                            .put("tag",orename)
                            .put("entries",entries)
                        );
        }
        Jsonify.writeOutput(new JSONObject().put("ore_dictionary",orenames),"./oredictionary.json");
    }

    public static void exportAspects(){
        JSONArray out = new JSONArray();
        long i = 0;
        long j = 0;
        for(ItemStack stack : new MasterItemList()){
            try{
                out.put(new JSONObject()
                        .put("aspects",Jsonify.aspectList(
                                ThaumcraftApiHelper.getObjectAspects(stack)
                                )
                            )
                        .put("item",Jsonify.item(stack))
                        );
            } catch ( Exception e){
                System.out.println("Aspect query failed for " + stack.getDisplayName());
                e.printStackTrace();
            }
            i++;
            if(i > j*(ITEM_COUNT/10)){
                System.out.println(j + "0% item aspects complete");
                j++;
            }
        }
        Jsonify.writeOutput(
                new JSONObject()
                .put("aspects",out)
                ,"./aspects.json"
                );
    }

    public static void exportGUIs(ICommandSender sender){
        new File("dumps").mkdirs();
        new File("dumps/guis").mkdirs();
        for (ICraftingHandler handler : craftinghandlers) {
            try {
                if (handler instanceof TemplateRecipeHandler) {
                    TemplateRecipeHandler templateRecipeHandler = (TemplateRecipeHandler) handler;
                    if (templateRecipeHandler.getGuiTexture() == null) continue;
                    IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(templateRecipeHandler.getGuiTexture()));
                    int dotLoc = templateRecipeHandler.getGuiTexture().lastIndexOf('.');
                    String ext = dotLoc < 0 ? "" : templateRecipeHandler.getGuiTexture().substring(dotLoc);
                    String aPath = templateRecipeHandler.getHandlerId() + "@@" + templateRecipeHandler.getRecipeName();
                    Files.copy(resource.getInputStream(), Paths.get("dumps/guis",  aPath + ext));
                }
            } catch (Exception e) {
                System.out.println(handler.getRecipeName() + ":" + handler.getHandlerId() + " failed");
                sender.addChatMessage( new ChatComponentText(handler.getRecipeName() + ":" + handler.getHandlerId() + " failed"));
            }
        }
    }
    
    
}
