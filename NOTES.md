Client Item
- Composite
    - Condition EMISSIVE
        - true: paint_brush_stalk_emissive (with tint for sparkles)
        - false: paint_brush_stalk
    - Condition RGB_COLOR
        - true: paint_brush_tip (with tint)
        - false: empty


BlockStateModel.collectParts()
BlockStateModelPart.getQuads()

QuadTemplate (cached):
- 4 Vector3fc
- Direction
- rows
- cols

QuadInstance: flat int[] for pixels + BitSet for emissive??

Chunk data attachment: Map<BlockPos, ???>

wait wtf how server will access model data in the first place oh shiiiitttttttttttt!!?!?!