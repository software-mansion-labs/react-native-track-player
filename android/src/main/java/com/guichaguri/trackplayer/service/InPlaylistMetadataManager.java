package com.guichaguri.trackplayer.service;

import android.util.Log;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InPlaylistMetadataManager {
    public static class InPlaylistEvent {
        public static class InvalidInPlaylistFormatException extends Exception {
            public InvalidInPlaylistFormatException(String message) {
                super(message);
            }
        }

        private final static String DATERANGE = "#EXT-X-DATERANGE:";
        private final String id;
        private final Instant startDate;
        private final Map<String, String> attributes;

        public InPlaylistEvent(String dateRangeEvent) throws InvalidInPlaylistFormatException {
            if (!dateRangeEvent.startsWith(DATERANGE)) {
                throw new InvalidInPlaylistFormatException("#EXT-X-DATERANGE tag not found");
            }

            String paramsString = dateRangeEvent.substring(DATERANGE.length());

            Map<String, String> processedParams = new HashMap<>();

            String[] rawParams = paramsString.split(",");
            for (String param: rawParams) {
                parseParam(processedParams, param);
            }

            if (!processedParams.containsKey("ID")) {
                throw new InvalidInPlaylistFormatException("'ID' field is missing");
            }
            this.id = processedParams.get("ID");

            if (!processedParams.containsKey("START-DATE")) {
                throw new InvalidInPlaylistFormatException("'START-DATE' field is missing");
            }
            try {
                this.startDate = Instant.parse(processedParams.get("START-DATE"));
            } catch (DateTimeParseException e) {
                throw new InvalidInPlaylistFormatException("Invalid 'START-DATE' format, expecting valid ISO UTC");
            }

            this.attributes = new HashMap<>();

            processedParams.forEach((key, value) -> {
                if (key.startsWith("X")) {
                    attributes.put(key, value);
                }
            });
        }

        public static boolean isDateRangeEvent(String event) {
            return event.startsWith(DATERANGE);
        }

        public String getId() {
            return id;
        }

        public Instant getStartDate() {
            return startDate;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        private void parseParam(Map<String, String> params, String param) {
            String[] splitted = param.split("=");
            if (splitted.length == 2) {
                String key = splitted[0];
                String value = splitted[1];

                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                params.put(key, value);
            }
        }
    }

    public InPlaylistMetadataManager() {}

    private final Set<String> registeredEvents = new HashSet<>();
    private final List<InPlaylistEvent> awaitingDateRangeEvents = new ArrayList<>();


    public List<InPlaylistEvent> getAwaitingDateRangeEvents() {
        return awaitingDateRangeEvents;
    }

    public void reset() {
        this.awaitingDateRangeEvents.clear();
        this.registeredEvents.clear();
    }

    public void removeAwaitingEvents(List<String> eventIds) {
        synchronized (this) {
            awaitingDateRangeEvents.removeIf(event -> eventIds.contains(event.getId()));
        }
    }
    

    public void processTags(List<String> tags) {
        for (String tag : tags) {
            if (!InPlaylistEvent.isDateRangeEvent(tag)) {
                continue;
            }

            InPlaylistEvent event = null;
            try {
                event = new InPlaylistEvent(tag);
            } catch (InPlaylistEvent.InvalidInPlaylistFormatException e) {
                e.printStackTrace();
                continue;
            }

            if (!registeredEvents.contains(event.getId())) {
                Log.d(Utils.LOG, "Registering in-playlist event");

                synchronized (this) {
                    registeredEvents.add(event.getId());
                    awaitingDateRangeEvents.add(event);
                }
            }

        }
    }
}