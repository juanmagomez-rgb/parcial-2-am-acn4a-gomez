package com.example.mispeliculas;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;

/** Comprobación online optativa; no requiere token ni altera la lista de películas. */
@RunWith(AndroidJUnit4.class)
public class GlideInstrumentedTest {
    @Test public void posterHttpsSeCargaYSeRecuperaDesdeCache() throws Exception {
        Assume.assumeTrue("Habilitar con argumento tmdbOnline=true y conexión a Internet",
                "true".equals(InstrumentationRegistry.getArguments().getString("tmdbOnline")));
        Context contexto = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Ruta del ejemplo público de la documentación de imágenes de TMDB.
        String url = Tmdb.urlPoster("/1E5baAaEse26fej7uHcjOgEE2t2.jpg");
        FutureTarget<Bitmap> descarga = Glide.with(contexto).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).submit(100, 150);
        try {
            assertTrue(descarga.get(35, TimeUnit.SECONDS).getWidth() > 0);
        } finally { Glide.with(contexto).clear(descarga); }
        FutureTarget<Bitmap> cache = Glide.with(contexto).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).onlyRetrieveFromCache(true).submit(100, 150);
        try {
            assertTrue(cache.get(10, TimeUnit.SECONDS).getHeight() > 0);
        } finally { Glide.with(contexto).clear(cache); }
    }
}
