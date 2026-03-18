package com.samhill.wildyslayerpicker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.samhill.wildyslayerpicker.model.WorldObservation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON serialization for observations (Gson used by RuneLite).
 */
public final class JsonUtil
{
	private static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
		.create();

	private static final Type OBSERVATION_LIST_TYPE = new TypeToken<ArrayList<WorldObservation>>() {}.getType();

	private JsonUtil() {}

	public static String observationsToJson(List<WorldObservation> observations)
	{
		return GSON.toJson(observations != null ? observations : new ArrayList<>());
	}

	public static List<WorldObservation> observationsFromJson(String json)
	{
		if (json == null || json.isBlank())
		{
			return new ArrayList<>();
		}
		try
		{
			List<WorldObservation> list = GSON.fromJson(json, OBSERVATION_LIST_TYPE);
			return list != null ? list : new ArrayList<>();
		}
		catch (Exception e)
		{
			return new ArrayList<>();
		}
	}

	/** Gson type adapter for java.time.Instant (ISO-8601). */
	public static class InstantTypeAdapter extends com.google.gson.TypeAdapter<Instant>
	{
		@Override
		public void write(com.google.gson.stream.JsonWriter out, Instant value) throws java.io.IOException
		{
			out.value(value != null ? value.toString() : null);
		}

		@Override
		public Instant read(com.google.gson.stream.JsonReader in) throws java.io.IOException
		{
			String s = in.nextString();
			return s == null || s.isEmpty() ? null : Instant.parse(s);
		}
	}
}
