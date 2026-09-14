import bpy
import os
import sys
import mathutils

# 解决中文路径/文件名乱码
import locale
try:
    locale.setlocale(locale.LC_ALL, 'zh_CN.UTF-8')
except:
    locale.setlocale(locale.LC_ALL, '')

# 【关键修复】正确提取外部传入的目标文件夹参数
# Blender 启动参数格式：blender --background --python 脚本.py -- 自定义参数
# sys.argv 中，-- 之后的才是我们传入的目标文件夹
target_dir = ""
for i, arg in enumerate(sys.argv):
    if arg == "--" and i+1 < len(sys.argv):
        target_dir = sys.argv[i+1]
        break

# 验证目标文件夹
if not target_dir or not os.path.exists(target_dir):
    print(f"错误：目标文件夹不存在或参数传递失败 → {target_dir}")
    sys.exit(1)

# 遍历所有 OBJ 文件（忽略大小写）
obj_files = [
    f for f in os.listdir(target_dir) 
    if f.lower().endswith('.obj') and os.path.isfile(os.path.join(target_dir, f))
]

if not obj_files:
    print(f"提示：{target_dir} 下未找到 .obj 文件")
    sys.exit(0)

print(f"找到 {len(obj_files)} 个 OBJ 文件，开始转换...\n")

# 批量转换逻辑
success_count = 0
fail_count = 0

for obj_file in obj_files:
    # 清空场景（关键：避免模型残留）
    bpy.ops.object.select_all(action='SELECT')
    bpy.ops.object.delete()
    bpy.ops.outliner.orphans_purge(do_local_ids=True, do_linked_ids=True)

    # 构建文件路径
    obj_path = os.path.join(target_dir, obj_file)
    ply_filename = os.path.splitext(obj_file)[0] + ".ply"
    ply_path = os.path.join(target_dir, ply_filename)

    try:
        # 导入 OBJ（兼容不同编码的 OBJ 文件）
        bpy.ops.import_scene.obj(
            filepath=obj_path,
            use_edges=True,
            use_smooth_groups=False,
            use_split_objects=False,
            use_split_groups=False
        )

        # OBJ 坐标系: Y+向上, Z+向前 -> Blender: Z+向上, Y+向前 (已自动转换)
        # 需要再旋转 -90° 绕 X 轴，使 Y+变为 Z+ (Z 轴朝上)
        bpy.ops.object.select_all(action='SELECT')
        for obj in bpy.context.selected_objects:
            if obj.type == 'MESH':
                bpy.context.view_layer.objects.active = obj
                # 直接修改顶点坐标：旋转 -90° 绕 X 轴
                import math
                matrix_rotation = mathutils.Matrix.Rotation(math.radians(-90), 4, 'X')
                obj.data.transform(matrix_rotation)

        # 导出二进制 PLY（核心参数）
        # Blender 2.93 PLY 导出可用参数（不包含 use_uvs/use_normals/format，始终导出为二进制）
        bpy.ops.export_mesh.ply(
            filepath=ply_path,
            check_existing=True,
            use_selection=False,
            use_colors=True
        )

        print(f"✅ 成功：{obj_file} → {ply_filename}")
        success_count += 1

    except Exception as e:
        print(f"❌ 失败：{obj_file} → 错误：{str(e)}")
        fail_count += 1

# 输出汇总
print(f"\n===== 转换完成 ======")
print(f"成功：{success_count} 个 | 失败：{fail_count} 个")
print(f"目标文件夹：{target_dir}")