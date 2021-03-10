package dev.lucasnlm.antimine.gdx

import com.badlogic.gdx.Application
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

class LevelApplicationListener : ApplicationAdapter() {
    override fun create() {
        super.create()
        Gdx.graphics.isContinuousRendering = false
    }

    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
