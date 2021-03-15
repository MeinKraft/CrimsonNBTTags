package crimsonfluff.CrimsonNBT.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import crimsonfluff.CrimsonNBT.CrimsonNBT;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class nbtCommands {
    // tried SlotArgument.slot() - it has armor.chest, hotbar0 etc but lots of others non-applicable, enderchest, horse, container etc
    // decided to keep simple with 0-40, -1 for current item

    public nbtCommands(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("crimsonnbt").requires((p_198496_0_) -> {
            return p_198496_0_.hasPermissionLevel(2);
        })
            .then(Commands.literal("copy")
                .then(Commands.argument("slot", IntegerArgumentType.integer(0, 40))
                .executes((p_198495_0_) -> {
                return nbtCopy(p_198495_0_, IntegerArgumentType.getInteger(p_198495_0_, "slot")); })))

            .then(Commands.literal("copy")
                .then(Commands.literal("current_item")
                .executes((p_198495_0_) -> {
                return nbtCopy(p_198495_0_,-1); })))
        );
    }

    private int nbtCopy(CommandContext<CommandSource> cscc, int slot) throws CommandSyntaxException {
        PlayerEntity player = cscc.getSource().asPlayer();
        //player.sendStatusMessage(new StringTextComponent("Hello world"), false);
        // TODO: Try cscc.sendFeedback ?

        ItemStack item;
        String myString;

        if (slot == -1)
            item = player.inventory.getCurrentItem();
        else
            item = player.inventory.getStackInSlot(slot);     // 0 to 40, includes armour, off hand

        if (item.isEmpty()) {
            player.sendStatusMessage(new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".empty"), false);

        } else {
            if (item.hasTag()) {
                myString = item.getTag().getString();

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(myString), null);

                myString = new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".copied").getString();

            } else
                myString = new TranslationTextComponent("tip." + CrimsonNBT.MOD_ID + ".notcopied").getString();

            if (item.hasDisplayName())
                player.sendStatusMessage(new StringTextComponent("'" + item.getDisplayName().getString()).appendString("' " + myString), false);
            else
                player.sendStatusMessage(new TranslationTextComponent(item.getTranslationKey()).appendString(" " + myString), false);
        }

        return 0;
    }
}
