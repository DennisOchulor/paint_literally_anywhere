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
- Generate accurate QuadTemplates on client
  - Write to JSON files
  - Deal with vanilla hitPos still using inaccurate VoxelShape
- Config (resolution)
- Somehow transfer pixels between QuadTemplates that are just rotated (e.g. flipping trapdoors)

---

**ToDo (bugs):**
- Some blocks (e.g. chests, piglin head) generate no quads?
- Some models like cross model share same quad?, which causes problems
- Fix skull block culling issues
- rightClickDelay packet spam
- QuadTemplate#clip is still sometimes unreliable
    - Lectern back top face
    - Composter, hopper, cauldron inner faces (hitDirection is UP for some reason...)
    - Removing the direction check optimization can lead to wrong quad being clipped??