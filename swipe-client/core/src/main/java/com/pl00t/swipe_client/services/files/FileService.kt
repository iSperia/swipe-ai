package com.pl00t.swipe_client.services.files

import com.badlogic.gdx.Gdx

interface FileService {

    fun localFile(name: String): String?
    fun internalFile(name: String): String?
}

class GdxFileService: FileService {

    override fun internalFile(name: String): String? {
        val handle = Gdx.files.internal(name)
        return try {
            handle.readString()
        } catch (t: Throwable) {
            null
        }
    }

    override fun localFile(name: String): String? {
        val handle = Gdx.files.local(name)
        return try {
            handle.readString()
        } catch (t: Throwable) {
            null
        }
    }
}
