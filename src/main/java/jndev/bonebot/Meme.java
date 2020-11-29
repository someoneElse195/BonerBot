package jndev.bonebot;

import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.text.WordUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * meme generator
 *
 * @author JNDev (Jeremaster101)
 */
public class Meme {
    
    /**
     * list of text lines loaded from the text file
     */
    public static final ArrayList<String> texts = new ArrayList<>();
    
    /**
     * number of memes created. helpful to separate meme files to prevent overwriting a meme being processed
     */
    private static int memeCount;
    
    /**
     * meme text
     */
    private String text;
    
    /**
     * background image
     */
    private BufferedImage image;
    
    /**
     * generated meme image
     */
    private BufferedImage meme;
    
    /**
     * command message
     */
    private final Message command;
    
    /**
     * constructs a new meme generator class with the original message for get command arguments from
     *
     * @param command command containing meme arguments
     */
    private Meme(Message command) {
        this.command = command;
    }
    
    /**
     * generate and send a meme
     *
     * @param command command entered by user
     */
    public static void generate(Message command) {
        Meme m = new Meme(command);
        m.generate();
    }
    
    /**
     * generate and send a meme
     */
    private void generate() {
        try {
            command.getChannel().sendTyping().queue();
            readTextAndImage();
            processImage();
            File file = convertToFile();
            command.getChannel().sendFile(file).queue();
            image.flush();
            meme.flush();
            image = null;
            meme = null;
            text = null;
            file.delete();
            System.gc();
        } catch (IOException | FontFormatException exception) {
            command.getChannel().sendMessage("Error generating meme! " +
                    command.getJDA().getUserByTag("Jeremaster101#0494").getAsMention()).queue();
            Logger.log(exception);
        }
    }
    
    /**
     * read an image from a discord message, user mention, or a random to use to generate a meme, as well as grab the
     * text from the message or use a random to make a meme
     *
     * @throws IOException
     */
    private void readTextAndImage() throws IOException {
        String input = command.getContentRaw().replaceFirst("bbmeme", "").trim();
        if (command.getAttachments().size() > 0 && command.getAttachments().get(0).isImage()) {
            this.image = ImageIO.read(new URL(command.getAttachments().get(0).getUrl()));
        } else if (command.getMentionedUsers().size() > 0) {
            this.image = ImageIO.read(new URL(command.getMentionedUsers().get(0).getEffectiveAvatarUrl()));
            for (int i = 0; i < command.getMentionedUsers().size(); i++)
                input = input.replace(command.getMentionedUsers().get(i).getAsMention(), "")
                        .replace("<@!" + command.getMentionedUsers().get(i).getIdLong() + ">", "")
                        .replace("  ", " ").trim();
        } else {
            Random r = new Random();
            File dir = new File("images");
            int rand = r.nextInt(dir.listFiles().length);
            while (dir.listFiles()[rand].isHidden()) {
                rand = r.nextInt(dir.listFiles().length);
            }
            this.image = ImageIO.read(dir.listFiles()[rand]);
        }
        if (!input.isEmpty() || !input.equals("")) {
            this.text = input;
        } else {
            Random r = new Random();
            this.text = texts.get(r.nextInt(texts.size()));
        }
    }
    
    /**
     * generate a meme using the input text and image
     */
    private void processImage() throws IOException, FontFormatException {
        double ratio = image.getHeight() / (double) image.getWidth();
        int width = 1024;
        int height = (int) (width * ratio);
        meme = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = meme.getGraphics();
        Graphics2D g2d = (Graphics2D) graphics;
        File fontFile = new File("/System/Library/Fonts/Supplemental/Impact.ttf");
        Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(96f);
        graphics.setFont(font);
        FontMetrics metrics = graphics.getFontMetrics(font);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, width, height, null);
        ArrayList<String> lines = new ArrayList<>();
        String[] sections = text.split("\n");
        for (String section : sections)
            lines.addAll(Arrays.asList(
                    WordUtils.wrap(section, 18, "\n", true).split("\n")));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim().toUpperCase();
            if (line.isEmpty()) continue;
            graphics.setColor(Color.WHITE);
            graphics.drawString(line,
                    (int) ((meme.getWidth(null) - metrics.stringWidth(line)) / 2.0),
                    (int) (meme.getHeight(null) - (lines.size() - i - 0.75) * graphics.getFont().getSize()));
            Shape shape = new TextLayout(line, font, g2d.getFontRenderContext()).getOutline(null);
            g2d.setStroke(new BasicStroke(3f));
            g2d.translate((int) ((meme.getWidth(null) - metrics.stringWidth(line)) / 2.0),
                    (int) (meme.getHeight(null) - (lines.size() - i - 0.75) * graphics.getFont().getSize()));
            graphics.setColor(Color.BLACK);
            g2d.draw(shape);
            g2d.translate((int) -((meme.getWidth(null) - metrics.stringWidth(line)) / 2.0),
                    (int) -(meme.getHeight(null) - (lines.size() - i - 0.75) * graphics.getFont().getSize()));
        }
        graphics.dispose();
        g2d.dispose();
    }
    
    /**
     * convert the meme image to a file, save it to the temp folder, and set it to auto delete later
     *
     * @return meme image converted to file
     * @throws IOException
     */
    private File convertToFile() throws IOException {
        File file = new File("temp/meme" + memeCount + ".jpg");
        ImageIO.write(meme, "jpg", file);
        memeCount++;
        return file;
    }
}
