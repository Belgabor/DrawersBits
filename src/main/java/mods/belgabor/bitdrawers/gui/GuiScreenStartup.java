package mods.belgabor.bitdrawers.gui;

import mods.belgabor.bitdrawers.BitDrawers;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by Belgabor on 14.11.2016.
 * Adapted from flatcoloredblocks by AlgortihmX2
 */
public class GuiScreenStartup extends GuiScreen
{
    String[] lines;

    @Override
    public void initGui() {
        final String msga = I18n.translateToLocal( "flatcoloredblocks.startup_a" );
        final String msgb = I18n.translateToLocal( "flatcoloredblocks.startup_b" );
        final String msgc = I18n.translateToLocal( "flatcoloredblocks.startup_c" );
        final String msgd = I18n.translateToLocal( "flatcoloredblocks.startup_d" );
        final String msge = I18n.translateToLocal( "flatcoloredblocks.startup_e" );
        final String msgf = I18n.translateToLocal( "flatcoloredblocks.startup_f" );

        String msg = I18n.translateToLocal("bitDrawers.startup_a") + "\n\n";
        msg += I18n.translateToLocalFormatted("bitDrawers.startup_b", BitDrawers.SD_VERSION, BitDrawers.detectedSdVersion) + "\n";
        if (BitDrawers.sdVersionCheckFailed) {
            msg += I18n.translateToLocal("bitDrawers.startup_fubar") + "\n\n";
        } else if (BitDrawers.sdMajorMismatch) {
            msg += I18n.translateToLocal("bitDrawers.startup_maj") + "\n\n";
        } else if (BitDrawers.sdMinorMismatch) {
            msg += I18n.translateToLocal("bitDrawers.startup_min") + "\n\n";
        }
        msg += I18n.translateToLocal("bitDrawers.startup_c") + "\n\n";
        msg += I18n.translateToLocal("bitDrawers.startup_d") + "\n\n";
        msg += I18n.translateToLocal("bitDrawers.startup_e");

        lines = msg.split( "\n" );

        buttonList.add( new GuiButton( 0, width / 2 - 144 / 2, height / 2 + 96, 144, 20, "Ok" ) );
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks ) {
        GL11.glEnable( GL11.GL_TEXTURE_2D );
        drawDefaultBackground();

        int heightLoc = 90;
        drawCenteredString( fontRendererObj, TextFormatting.YELLOW + "Drawers & Bits", width / 2, height / 2 - 110, 0xFFFFFF );

        for ( final String s : lines )
        {
            final List<String> info = fontRendererObj.listFormattedStringToWidth( s, width - 40 );
            for ( final String infoCut : info )
            {
                drawCenteredString( fontRendererObj, infoCut, width / 2, height / 2 - heightLoc, 0xFFFFFF );
                heightLoc = heightLoc - 12;
            }
        }

        super.drawScreen( mouseX, mouseY, partialTicks );
    }

    @Override
    public void actionPerformed(final GuiButton button ) {
        switch (button.id) {
            case 0: {
                for (final GuiButton b : buttonList) {
                    b.enabled = false;
                }

                if (!BitDrawers.detectedSdVersion.equals(""))
                    BitDrawers.config.updateSDVersion();

                mc.displayGuiScreen( null );

                break;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
