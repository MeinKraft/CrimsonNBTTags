package crimsonfluff.CrimsonNBT;

import com.sun.java.accessibility.util.java.awt.TextComponentTranslator;
import crimsonfluff.CrimsonNBT.util.KeyboardHelper;
import crimsonfluff.CrimsonNBT.util.nbtCommands;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Collection;

@Mod(CrimsonNBT.MOD_ID)
public class CrimsonNBT {
    public static final String MOD_ID = "crimsonnbt";

    public CrimsonNBT() {
        // https://forums.minecraftforge.net/topic/96248-set-clipboard/
        System.setProperty("java.awt.headless", "false");       // Stops command failing because of error

        MinecraftForge.EVENT_BUS.register(this);
    }

// removed the class
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void ItemToolTipEvent(ItemTooltipEvent event) {
        if (!Minecraft.getInstance().gameSettings.advancedItemTooltips) return;

        ItemStack current = event.getItemStack();
        if (current.isEmpty()) return;

// Add Burntime: "148940 (2 hours, 4 minutes, 7 seconds)"
// 148940 = (((2*60)+4)*60+7)*20
        int BurnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(current);
        if (BurnTime > 0) {
            String timeString = BurnTime + " (";
            BurnTime /= 20;

            int hours = BurnTime / 3600;
            BurnTime %= 3600;
            int mins = BurnTime / 60;
            int secs = BurnTime % 60;

            if (hours > 0)
                timeString = timeString + hours + " " + new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".hours").getString();
            if (mins > 0) {
                if (hours > 0) timeString = timeString + ", ";
                timeString = timeString + mins + " " + new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".minutes").getString();
            }
            if (secs > 0) {
                if (mins > 0) timeString = timeString + ", ";
                timeString = timeString + secs + " " + new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".seconds").getString();
            }

            event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".burn").appendString(": " + timeString +")").mergeStyle(TextFormatting.DARK_GRAY));
        }

// NOTE: Check .hasTag first !
        if (current.hasTag()) {
            if (current.getMaxDamage() != 0 && current.getDamage() == 0)
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".durability").appendString(": " + current.getMaxDamage()).mergeStyle(TextFormatting.DARK_GRAY));

            if (KeyboardHelper.isHoldingCtrl()) {
                String st = current.getTag().toString();
                int l = 200;

                if (st.length() > l) {
                    event.getToolTip().add(new StringTextComponent(st.substring(0, l)).mergeStyle(TextFormatting.DARK_GRAY));
                    event.getToolTip().add(new StringTextComponent((st.length() - l) + " " + new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".more")).mergeStyle(TextFormatting.DARK_GRAY));

                } else event.getToolTip().add(new StringTextComponent(st).mergeStyle(TextFormatting.DARK_GRAY));

            } else event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".ctrl"));
        }

        // Don't generate block tags: just checking if *any* TAGs exist <- uses less resources ?!
        // also re-use iTag
        Collection<ResourceLocation> iTag = ItemTags.getCollection().getOwningTags(current.getItem());
        if (KeyboardHelper.isHoldingShift()) {
            if (iTag.size() > 0) {
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".item_tags").mergeStyle(TextFormatting.GRAY));

                for (ResourceLocation tag : iTag)
                    event.getToolTip().add(new StringTextComponent("  #" + tag).mergeStyle(TextFormatting.DARK_GRAY));
            }

            iTag = BlockTags.getCollection().getOwningTags(Block.getBlockFromItem(current.getItem()));
            if (iTag.size() > 0) {
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".block_tags").mergeStyle(TextFormatting.GRAY));

                for (ResourceLocation tag : iTag)
                    event.getToolTip().add(new StringTextComponent("  #" + tag).mergeStyle(TextFormatting.DARK_GRAY));
            }

        } else {
            if (iTag.size() == 0) iTag = BlockTags.getCollection().getOwningTags(Block.getBlockFromItem(current.getItem()));

            if ((iTag.size()) > 0)
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".shift"));
        }

// add ModName to end of Tooltip (mainly for when hovering over items in Vanilla GUIs)
// JEI seems to be only mod that adds ModName to the tooltip
// no check needed if ModName is already in tooltip, maybe JEI does this itself? didn't duplicate name in tooltip during testing
        String modName = getModName(current);
        if (modName != null)
            //if (!event.getToolTip().get(event.getToolTip().size() - 1).getString().equals(modID))
                event.getToolTip().add(new StringTextComponent(modName).mergeStyle(TextFormatting.BLUE));
    }

    @Nullable
    private static String getModName(ItemStack itemStack) {
// already checks for isEmpty() at start of ToolTip Event
        String modName = itemStack.getItem().getCreatorModId(itemStack);

        if (modName != null) {
            return ModList.get().getModContainerById(modName)
                .map(modContainer -> modContainer.getModInfo().getDisplayName())
                .orElse(StringUtils.capitalize(modName));
        }

        return null;
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) { new nbtCommands(event.getDispatcher()); }
}
