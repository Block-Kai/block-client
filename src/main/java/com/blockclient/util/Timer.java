package com.blockclient.util;

/**
 * 计时器工具
 */
public class Timer {
    private long startMs = System.currentTimeMillis();
    private long startNs = System.nanoTime();

    public void reset() {
        startMs = System.currentTimeMillis();
        startNs = System.nanoTime();
    }

    /** 是否经过了指定毫秒数 */
    public boolean passed(long ms) {
        return passedMs(ms);
    }

    public boolean passedMs(long ms) {
        return System.currentTimeMillis() - startMs >= ms;
    }

    /** 是否经过了指定纳秒数 */
    public boolean passedNs(long ns) {
        return System.nanoTime() - startNs >= ns;
    }

    /** 获取经过的毫秒数 */
    public long getMs() {
        return System.currentTimeMillis() - startMs;
    }

    /** 获取经过的秒数 */
    public double getSeconds() {
        return (System.currentTimeMillis() - startMs) / 1000.0;
    }
}
