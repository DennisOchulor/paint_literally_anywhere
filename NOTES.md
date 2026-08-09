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
- Add eraser
- Generate accurate QuadTemplates on client
  - Optimize read/write of shape files
- Config (resolution)?
- Somehow transfer pixels between QuadTemplates that are just rotated (e.g. flipping trapdoors)

---

**ToDo (bugs):**
- BEs that render entirely via BER still use VoxelShape
- Some models like cross model share same quad?, which causes problems
- Fix skull block culling issues
- rightClickDelay packet spam
- Revisit color picker interactions
- QuadTemplate#clip is still sometimes unreliable
    - Lectern back top face
    - Sign sides/top