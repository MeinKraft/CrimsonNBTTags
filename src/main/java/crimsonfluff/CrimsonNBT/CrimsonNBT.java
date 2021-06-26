package crimsonfluff.CrimsonNBT;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

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
        if (! Minecraft.getInstance().options.advancedItemTooltips) return;

        ItemStack current = event.getItemStack();
        if (current.isEmpty()) return;

// Add Burntime: "148940 (2 hours, 4 minutes, 7 seconds)"
// 148940 = (((2*60)+4)*60+7)*20
        int BurnTime = ForgeHooks.getBurnTime(current);
        if (BurnTime > 0) {
            String timeString = BurnTime + " (";
            BurnTime /= 20;

            int hours = BurnTime / 3600;
            BurnTime %= 3600;
            int mins = BurnTime / 60;
            int secs = BurnTime % 60;  // TODO: Reuse BurnTime variable

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

            event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".burn").append(": " + timeString + ")").withStyle(TextFormatting.DARK_GRAY));
        }

        if (current.hasTag()) {
            if (current.getMaxDamage() != 0 && current.getDamageValue() == 0)
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".durability").append(": " + current.getMaxDamage()).withStyle(TextFormatting.DARK_GRAY));

            if (isHoldingCtrl()) {
                String st = current.getTag().toString();
                int l = 200;

                if (st.length() > l) {
                    event.getToolTip().add(new StringTextComponent(st.substring(0, l)).withStyle(TextFormatting.DARK_GRAY));
                    event.getToolTip().add(new StringTextComponent((st.length() - l) + " ").append(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".more")).withStyle(TextFormatting.DARK_GRAY));

                } else event.getToolTip().add(new StringTextComponent(st).withStyle(TextFormatting.DARK_GRAY));

            } else
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".ctrl", new TranslationTextComponent("tip.ctrl").withStyle(TextFormatting.YELLOW)).withStyle(TextFormatting.GRAY));
        }

        Collection<ResourceLocation> iTag = ItemTags.getAllTags().getMatchingTags(current.getItem());
        if (isHoldingShift()) {
            if (iTag.size() > 0) {
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".item_tags").withStyle(TextFormatting.GRAY));

                for (ResourceLocation tag : iTag)
                    event.getToolTip().add(new StringTextComponent("  #" + tag).withStyle(TextFormatting.DARK_GRAY));
            }

            iTag = BlockTags.getAllTags().getMatchingTags(Block.byItem(current.getItem()));
            if (iTag.size() > 0) {
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".block_tags").withStyle(TextFormatting.GRAY));

                for (ResourceLocation tag : iTag)
                    event.getToolTip().add(new StringTextComponent("  #" + tag).withStyle(TextFormatting.DARK_GRAY));
            }

        } else {
            if (iTag.size() == 0) iTag = BlockTags.getAllTags().getMatchingTags(Block.byItem(current.getItem()));

            if (iTag.size() > 0)
                event.getToolTip().add(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".shift", new TranslationTextComponent("tip.shift").withStyle(TextFormatting.YELLOW)).withStyle(TextFormatting.GRAY));
        }

// Must check else you get double mod name in JEI
// WAILA/HWYLA adds mod name too, so don't bother
        if (! ModList.get().isLoaded("waila")) {
            String modName = getModName(current);
            if (modName != null)
                if (! event.getToolTip().get(event.getToolTip().size() - 1).getString().equals(modName))
                    event.getToolTip().add(new StringTextComponent(modName).withStyle(TextFormatting.BLUE));
        }
    }

    @Nullable
    private static String getModName(ItemStack itemStack) {
        String modName = itemStack.getItem().getCreatorModId(itemStack);

        if (modName != null) {
            return ModList.get().getModContainerById(modName)
                .map(modContainer -> modContainer.getModInfo().getDisplayName())
                .orElse(StringUtils.capitalize(modName));
        }

        return null;
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean isHoldingShift() {
        long WINDOW = Minecraft.getInstance().getWindow().getWindow();
        return InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @OnlyIn(Dist.CLIENT)
    private static boolean isHoldingCtrl() {
        long WINDOW = Minecraft.getInstance().getWindow().getWindow();
        return InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_LEFT_CONTROL) || InputMappings.isKeyDown(WINDOW, GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) { new nbtCommands(event.getDispatcher()); }
}
