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
- Config (resolution)?
- Somehow transfer pixels when block state changes in certain ways
  - e.g. flipping trapdoors

---

**ToDo (bugs):**
- Some models like cross model share same quad?, which causes problems
- BEs that render entirely via BER still use VoxelShape
- Fix skull block culling issues
- rightClickDelay packet spam
- Revisit color picker interactions
- Revisit durability issues