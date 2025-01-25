package com.simibubi.create.content.kinetics.fan.processing;

import java.util.Collections;
import java.util.List;

import com.simibubi.create.AllRegistries;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import org.jetbrains.annotations.UnmodifiableView;

public class FanProcessingTypeRegistry {
	private static List<FanProcessingType> sortedTypes = null;
	@UnmodifiableView
	private static List<FanProcessingType> sortedTypesView = null;

	@UnmodifiableView
	public static List<FanProcessingType> getSortedTypesView() {
		if (sortedTypes == null) {
			sortedTypes = new ReferenceArrayList<>();

			sortedTypes.addAll(AllRegistries.FAN_PROCESSING_TYPES.get().getValues());
			sortedTypes.sort((t1, t2) -> t2.getPriority() - t1.getPriority());

			sortedTypesView = Collections.unmodifiableList(sortedTypes);
		}

		return sortedTypesView;
	}
}
