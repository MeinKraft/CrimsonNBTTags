package crimsonfluff.CrimsonNBT;

import crimsonfluff.CrimsonNBT.util.KeyboardHelper;
import crimsonfluff.CrimsonNBT.util.nbtCommands;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Collection;

@Mod(CrimsonNBT.MOD_ID)
public class CrimsonNBT {
    public static final String MOD_ID = "crimsonnbt";

    public CrimsonNBT() {
        // https://forums.minecraftforge.net/topic/96248-set-clipboard/
        System.setProperty("java.awt.headless", "false");       // Stops command failing because of error

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(modid = CrimsonNBT.MOD_ID, value = Dist.CLIENT)
    public static class ToolTipEvent {
        @SubscribeEvent
        public static void ItemToolTipEvent(ItemTooltipEvent event) {
            if (!Minecraft.getInstance().gameSettings.advancedItemTooltips) return;

            ItemStack current = event.getItemStack();
            if (current.isEmpty()) return;

        // NOTE: Check .hasTag first !
            if (current.hasTag()) {
                if (!KeyboardHelper.isHoldingCtrl())
                    event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".ctrl"));

                else {
                    String st = current.getTag().toString();
                    int l = 200;

                    if (st.length() > l) {
                        event.getToolTip().add(new StringTextComponent(st.substring(0, l)).mergeStyle(TextFormatting.DARK_GRAY));
                        event.getToolTip().add(new StringTextComponent((st.length() - l) + " more...").mergeStyle(TextFormatting.DARK_GRAY));

                    } else
                        event.getToolTip().add(new StringTextComponent(st).mergeStyle(TextFormatting.DARK_GRAY));
                }
            }

            // Don't generate block tags: just checking if *any* TAGs exist <- uses less resources ?!
            // also re-use iTag
            Collection<ResourceLocation> iTag = ItemTags.getCollection().getOwningTags(current.getItem());
            if (!KeyboardHelper.isHoldingShift()) {
                //Collection<ResourceLocation> iTag = ItemTags.getCollection().getOwningTags(current.getItem());
                if (iTag.size() == 0) iTag = BlockTags.getCollection().getOwningTags(Block.getBlockFromItem(current.getItem()));

                if ((iTag.size()) > 0)
                    event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".shift"));

            } else {
                //Collection<ResourceLocation> iTag = ItemTags.getCollection().getOwningTags(current.getItem());

                if (iTag.size() > 0) {
                    event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".item_tags").mergeStyle(TextFormatting.GRAY));

                    for (ResourceLocation tag : iTag) {
                        event.getToolTip().add(new StringTextComponent("  #" + tag).mergeStyle(TextFormatting.DARK_GRAY));
                    }
                }

                iTag = BlockTags.getCollection().getOwningTags(Block.getBlockFromItem(current.getItem()));
                if (iTag.size() > 0) {
                    event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".block_tags").mergeStyle(TextFormatting.GRAY));

                    for (ResourceLocation tag : iTag) {
                        event.getToolTip().add(new StringTextComponent("  #" + tag).mergeStyle(TextFormatting.DARK_GRAY));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) { new nbtCommands(event.getDispatcher()); }
}
