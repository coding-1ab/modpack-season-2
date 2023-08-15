package org.antarcticgardens.newage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class Configurations {

    public static int WIRE_SECTIONS_PER_METER = 10;
    public static float WIRE_THICKNESS = 0.03f;
    public static int MAX_RODS_IN_DIRECTION = 32;

    public static float SU_TO_ENERGY = 0.01f; // 0.2 energy per 1 su/s

    public static int MAX_COILS = 8;


    public static void load() throws IOException { // quick and easy config without having to depend on some config library
        File f = new File("config/create-new-age.txt");
        Map<String, String> configs = new HashMap<>();

        if (f.exists()) {
            String content = Files.readString(f.toPath());
            for (String line : content.split("\n")) {
                if (line.length() > 3 || line.startsWith("#")) {
                    String[] split = line.split(" ");
                    if (split.length > 1) {
                        configs.put(split[0], split[1]);
                    }
                }
            }
        } else {
            Files.write(f.toPath(), String.format(
                    """
                    # Responsible for how much energy is generated per 1 stress unit in a tick
                    # Set this setting to 0.029296875 for compat with the default config of create: crafts and additions
                    SU_TO_ENERGY %s
                    
                    # How many Reactor rods can a fuel inserter or a heat vent have into any one direction
                    MAX_RODS_IN_DIRECTION %s
                    
                    # How many coils can the carbon brushes collect energy from
                    MAX_COILS %s
                    
                    # Wire rendering settings
                    # settings this number to a lower number will improve fps
                    WIRE_SECTIONS_PER_METER 10
                    WIRE_THICKNESS 0.03
                    """,
                    SU_TO_ENERGY,
                    MAX_RODS_IN_DIRECTION,
                    MAX_COILS
            ).getBytes());
        }

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            try {
                switch (entry.getKey()) {
                    case "MAX_RODS_IN_DIRECTION" -> MAX_RODS_IN_DIRECTION = Integer.parseInt(entry.getValue());
                    case "SU_TO_ENERGY" -> SU_TO_ENERGY = Float.parseFloat(entry.getValue());
                    case "MAX_COILS" -> MAX_COILS = Integer.parseInt(entry.getValue());
                    case "WIRE_SECTIONS_PER_METER" -> WIRE_SECTIONS_PER_METER = Integer.parseInt(entry.getValue());
                    case "WIRE_THICKNESS" -> WIRE_THICKNESS = Float.parseFloat(entry.getValue());
                }
            } catch (Exception e) {
                CreateNewAge.LOGGER.error("Error setting " + entry.getKey() + " to " + entry.getValue(), e);
            }
        }

    }

}
