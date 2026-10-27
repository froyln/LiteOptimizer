package dev.froyln.liteoptimizer.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Optional reflection bridge; LiteOptimizer has no Tweakeroo runtime dependency. */
public final class TweakerooCompat {
    private static final Logger LOGGER = LogManager.getLogger("LiteOptimizer");
    private static final Object MAIN_THREAD_TOGGLE;
    private static final Method GET_BOOLEAN_VALUE;
    private static boolean freeCameraSpectator;
    private static boolean warned;

    static {
        Object toggle = null;
        Method getter = null;
        try {
            Class<?> featureToggle = Class.forName("fi.dy.masa.tweakeroo.config.FeatureToggle");
            Field field = featureToggle.getField("TWEAK_CHUNK_RENDER_MAIN_THREAD");
            toggle = field.get(null);
            getter = featureToggle.getMethod("getBooleanValue");
        } catch (ClassNotFoundException ignored) {
            // Tweakeroo is optional.
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOGGER.warn("Could not read Tweakeroo's immediate chunk-build setting", ex);
        }
        MAIN_THREAD_TOGGLE = toggle;
        GET_BOOLEAN_VALUE = getter;
    }

    private TweakerooCompat() {}

    public static void setFreeCameraSpectator(boolean spectator) {
        freeCameraSpectator = spectator;
    }

    public static boolean isFreeCameraSpectator() {
        return freeCameraSpectator;
    }

    public static boolean forceChunkBuildsOnMainThread() {
        if (MAIN_THREAD_TOGGLE == null || GET_BOOLEAN_VALUE == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(GET_BOOLEAN_VALUE.invoke(MAIN_THREAD_TOGGLE));
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (!warned) {
                LOGGER.warn("Could not read Tweakeroo's immediate chunk-build setting", ex);
                warned = true;
            }
            return false;
        }
    }
}
