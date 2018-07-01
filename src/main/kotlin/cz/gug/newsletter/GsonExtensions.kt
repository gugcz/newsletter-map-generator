package cz.gug.newsletter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Reader

inline fun <reified T : Any> Gson.fromJson(reader: Reader): T = fromJson(reader, object : TypeToken<T>() {}.type)