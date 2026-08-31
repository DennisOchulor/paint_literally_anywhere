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

Likely:
- Figure out dye algorithm
- Consider clicking images directly to select tool

---

Unlikely:
- Transfer pixels when block state changes in certain ways
  - e.g. flipping trapdoors
  - May not be possible without hardcoding to certain block state properties
- Silk touch to retain paint
  - Also may not be possible without hardcoding to certain block state properties
---

**ToDo (bugs):**
- BEs that render entirely via BER still use VoxelShape
  - see skulls, banners