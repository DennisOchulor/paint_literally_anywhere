package io.github.dennisochulor.paint_literally_anywhere.shape;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.dennisochulor.paint_literally_anywhere.OddCodecs;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushItem;
import io.github.dennisochulor.paint_literally_anywhere.item.PaintBrushProperties;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.IntStream;

public record QuadInstance(
        QuadTemplate template,
        int rows,
        int cols,
        int resolution,
        int[] pixels,
        BitSet emissiveData
) {
    public static final Codec<QuadInstance> CODEC = RecordCodecBuilder.create(
        instance ->
                instance.group(
                        QuadTemplate.CODEC.fieldOf("template").forGetter(QuadInstance::template),
                        Codec.INT.fieldOf("rows").forGetter(QuadInstance::rows),
                        Codec.INT.fieldOf("cols").forGetter(QuadInstance::cols),
                        Codec.INT.fieldOf("resolution").forGetter(QuadInstance::resolution),
                        OddCodecs.INT_ARRAY_CODEC.fieldOf("pixels").forGetter(QuadInstance::pixels),
                        ExtraCodecs.BIT_SET.fieldOf("emissiveData").forGetter(QuadInstance::emissiveData)
                ).apply(instance, QuadInstance::new)
    );

    public static final StreamCodec<ByteBuf, QuadInstance> STREAM_CODEC = StreamCodec.composite(
            QuadTemplate.STREAM_CODEC, QuadInstance::template,
            ByteBufCodecs.INT, QuadInstance::rows,
            ByteBufCodecs.INT, QuadInstance::cols,
            ByteBufCodecs.INT, QuadInstance::resolution,
            OddCodecs.INT_ARRAY_STREAM_CODEC, QuadInstance::pixels,
            OddCodecs.BIT_SET_STREAM_CODEC, QuadInstance::emissiveData,
            QuadInstance::new
    );

    private static final int[] EMPTY_ARRAY = new int[0];
    private static final PaintBrushItem.PaintResult EMPTY_RESULT = new PaintBrushItem.PaintResult(EMPTY_ARRAY, null);
    private static final PaintBrushItem.PaintResult DURABILITY_RESULT = new PaintBrushItem.PaintResult(EMPTY_ARRAY, "Insufficient durability");


    public QuadInstance(QuadTemplate template, int resolution) {
        float resPixelLength = 1.0F / resolution;
        int rows = (int) Math.ceil(template.rowVector().length() / resPixelLength);
        int cols = (int) Math.ceil(template.colVector().length() / resPixelLength);
        int numOfPixels = rows * cols;

        this(template, rows, cols, resolution, new int[numOfPixels], new BitSet(numOfPixels));
    }

    public record RowCol(int row, int col) {}

    public RowCol getRowCol(Vector3fc localHitPos) {
        // Find distance from a point to a line
        Vector3f v0ToHitPos = new Vector3f();
        localHitPos.sub(template.v0(), v0ToHitPos);

        Vector3f refVec = new Vector3f();
        float colLength = template.colVector().length();
        float rowLength = template.rowVector().length();

        // this is not backwards, e.g. distance from colVector (line at top) would give us the row
        float rowDistance = template.colVector().cross(v0ToHitPos, refVec).length() / colLength;
        float colDistance = template.rowVector().cross(v0ToHitPos, refVec).length() / rowLength;
        int col = (int) (colDistance / colLength * this.cols);
        int row = (int) (rowDistance / rowLength * this.rows);

        return new RowCol(row, col);
    }

    public int index(int row, int col) {
        return row * cols + col;
    }

    public int index(RowCol rowCol) {
        return index(rowCol.row(), rowCol.col());
    }

    /**
     * @return the painted index, or -1 if the pixel was already in the requested state.
     */
    public PaintBrushItem.PaintResult paintServer(Vector3fc localHitPos, PaintBrushProperties properties, int remainingDurability, boolean shouldUseDurability) {
        RowCol rowCol = getRowCol(localHitPos);
        int row = rowCol.row();
        int col = rowCol.col();
        int index = index(row, col);

        if (index >= pixels.length) {
            PLAMod.LOGGER.warn("Attempt to paint out-of-bounds index {} at {} for template {}", index, localHitPos, template);
            return EMPTY_RESULT;
        }

        int[] pixelsToPaint = properties.tool() == PaintBrushProperties.Tool.FILL ?
                getFillIndices(index, row, col, properties.argb(), properties.emissive()) : getBrushIndices(index, row, col, properties.brushSize());

        if (shouldUseDurability && pixelsToPaint.length > remainingDurability) {
            return DURABILITY_RESULT;
        }

        IntStream.Builder builder = IntStream.builder();
        for (int pixel : pixelsToPaint) {
            if (directPaint(pixel, properties.argb(), properties.emissive())) {
                builder.accept(pixel);
            }
        }

        int[] pixelsPainted = builder.build().toArray();
        return new PaintBrushItem.PaintResult(pixelsPainted, null);
    }

    public boolean directPaint(int index, int argb, boolean emissive) {
        if (pixels[index] == argb && emissive == emissiveData.get(index)) {
            return false;
        }

        pixels[index] = argb;
        emissiveData.set(index, emissive);
        return true;
    }

    private int[] getFillIndices(int index, int sr, int sc, int newColor, boolean emissive) {
        // https://www.geeksforgeeks.org/dsa/flood-fill-algorithm/

        // If the starting pixel already has the new color
        if (pixels[index] == newColor && emissive == emissiveData.get(index)) {
            return EMPTY_ARRAY;
        }

        // Direction vectors for traversing 4 directions
        int[][] dir = { {1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        Queue<int[]> q = new LinkedList<>();
        int oldColor = pixels[index];
        q.add(new int[]{sr, sc});

        // Add the starting pixel
        IntSet visitedPixels = new IntOpenHashSet();
        visitedPixels.add(index);

        // Perform BFS
        while (!q.isEmpty()) {
            int[] front = q.poll();
            int x = front[0], y = front[1];

            // Traverse all 4 directions
            for (int[] it : dir) {
                int nx = x + it[0];
                int ny = y + it[1];

                int i = index(nx, ny);

                // Check boundary conditions and color match
                if (nx >= 0 && nx < rows && ny >= 0 && ny < cols &&
                        pixels[i] == oldColor && !visitedPixels.contains(i))
                {
                    q.add(new int[]{nx, ny});
                    visitedPixels.add(i);
                }
            }
        }

        return visitedPixels.toIntArray();
    }

    private int[] getBrushIndices(int index, int row, int col, int brushSize) {
        if (brushSize == 1) return new int[]{index};

        // odd: floor(brushSize/2)
        // even: brushSize/2 - 1
        int stepsBack = brushSize % 2 == 0 ? brushSize / 2 - 1 : brushSize / 2;
        int uncheckedStartRow = row - stepsBack;
        int uncheckedStartCol = col - stepsBack;

        int startRow = Mth.clamp(uncheckedStartRow, 0, rows - 1);
        int startCol = Mth.clamp(uncheckedStartCol, 0, cols - 1);

        int underflowRow = uncheckedStartRow < 0 ? Math.abs(uncheckedStartRow) : 0;
        int underflowCol = uncheckedStartCol < 0 ? Math.abs(uncheckedStartCol) : 0;

        int endRow = Mth.clamp(startRow + brushSize - underflowRow - 1, 0, rows - 1);
        int endCol = Mth.clamp(startCol + brushSize - underflowCol - 1, 0, cols - 1);

        int size = (endRow - startRow + 1) * (endCol - startCol + 1);
        int[] pixelsToPaint = new int[size];

        int i = 0;
        for (int r = startRow; r <= endRow; r++) {
            for (int c = startCol; c <= endCol; c++) {
                pixelsToPaint[i] = index(r, c);
                i++;
            }
        }

        return pixelsToPaint;
    }
}
