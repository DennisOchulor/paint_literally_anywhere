Client Item
- Composite
    - Condition EMISSIVE
        - true: paint_brush_stalk_emissive (with tint for sparkles)
        - false: paint_brush_stalk
    - Condition RGB_COLOR
        - true: paint_brush_tip (with tint)
        - false: empty

---

**ToDo (features):**
- Setup default minecraft.dat file
- Consider clicking images directly to select tool
- Figure out dye algorithm
- Somehow transfer pixels when block state changes in certain ways
  - e.g. flipping trapdoors

---

**ToDo (bugs):**
- BEs that render entirely via BER still use VoxelShape
  - see skulls, banners