package xyz.amycute.powerchip.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.amycute.powerchip.PowerChips;
import xyz.amycute.powerchip.registry.ModItems;
import xyz.amycute.powerchip.util.ChipAssembly;

@EventBusSubscriber(modid = PowerChips.MOD_ID)
public final class ChipCommand
{
    private static final String KEY_PREFIX = PowerChips.MOD_ID + ".command.compile.";

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event)
    {
        event.getDispatcher().register(Commands.literal(PowerChips.MOD_ID).then(Commands.literal("compile").executes(ctx -> compileHeld(ctx.getSource()))));
    }

    private static boolean isChip(ItemStack stack)
    {
        for (var holder : ModItems.CHIPS.values()) if (stack.is(holder.get())) return true;
        return false;
    }

    private static int compileHeld(CommandSourceStack source) throws CommandSyntaxException
    {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack held = player.getMainHandItem();

        if (isChip(held))
        {
            source.sendFailure(Component.translatable(KEY_PREFIX + ChipAssembly.Failure.ALREADY_COMPILED));
            return 0;
        }

        ChipAssembly.Result result = ChipAssembly.convert(held, held.getCount());
        if (!result.ok())
        {
            source.sendFailure(Component.translatable(KEY_PREFIX + result.failure().key()));
            return 0;
        }

        ItemStack chip = result.stack();
        int count = chip.getCount();

        player.setItemInHand(InteractionHand.MAIN_HAND, chip);
        source.sendSuccess(() -> Component.translatable(KEY_PREFIX + "success", count), true);
        return count;
    }
}