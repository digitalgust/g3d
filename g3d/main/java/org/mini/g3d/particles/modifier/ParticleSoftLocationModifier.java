package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.MutableLocation;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;


/**
 * 改变粒子的位置, 使其柔和地跟随目标
 * 模拟阻尼或平滑移动效果
 */
public class ParticleSoftLocationModifier extends ParticleModifier {
    float startAt;
    float endAt;
    MutableLocation mutableLocation;
    
    // 内部状态
    Vector3f currentPos = null; // 当前追踪到的平滑位置
    Vector3f oldPos = null;     // 上一帧的平滑位置
    Vector3f diff = new Vector3f();
    
    // 参数
    float responsiveness = 5.0f; // 响应速度，越大越快，类似弹簧硬度

    long frameCount;

    @Override
    public void update(Particle particle) {
        if (mutableLocation == null) return;

        // 每帧只计算一次位移增量 (所有粒子共享这个增量)
        if (frameCount != DisplayManager.getFrameCount()) {
            frameCount = DisplayManager.getFrameCount();
            onNewFrame();
        }

        // 计算出相对时间差
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            Vector3f temp = particle.getPosition();
            temp.x += diff.x;
            temp.y += diff.y;
            temp.z += diff.z;
        }
    }

    private void onNewFrame() {
        // 获取目标的真实位置
        Vector3f targetPos = mutableLocation.getPosition();
        
        // 初始化
        if (currentPos == null) {
            currentPos = new Vector3f(targetPos);
            oldPos = new Vector3f(targetPos);
            return; // 第一帧没有移动
        }

        // 计算从 currentPos 向 targetPos 的平滑移动
        // 使用简单的 Lerp: newPos = current + (target - current) * factor
        // factor 取决于时间步长，保证帧率无关性
        float dt = DisplayManager.getFrameTimeSeconds();
        
        // 限制 dt 防止过大跳跃
        if (dt > 0.1f) dt = 0.1f;

        // 移动比例 factor = 1 - exp(-speed * dt) 是更精确的平滑公式，
        // 但简单的 factor = speed * dt 在小步长下也足够好用
        float factor = responsiveness * dt;
        if (factor > 1.0f) factor = 1.0f;

        // 更新 currentPos (向 targetPos 靠近)
        float dx = (targetPos.x - currentPos.x) * factor;
        float dy = (targetPos.y - currentPos.y) * factor;
        float dz = (targetPos.z - currentPos.z) * factor;
        
        currentPos.x += dx;
        currentPos.y += dy;
        currentPos.z += dz;

        // 计算 diff: 这一帧 currentPos 移动了多少
        diff.set(currentPos.x - oldPos.x, currentPos.y - oldPos.y, currentPos.z - oldPos.z);

        // 更新 oldPos 为下一帧做准备
        oldPos.set(currentPos);
    }

    /**
     * ======================================================
     * setter
     * ======================================================
     */

    public void setStartAt(float startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(float endAt) {
        this.endAt = endAt;
    }


    public void setMutableLocation(MutableLocation mutableLocation) {
        this.mutableLocation = mutableLocation;
    }
    
    public void setResponsiveness(float responsiveness) {
        this.responsiveness = responsiveness;
    }
}