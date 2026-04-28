import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatchConfig {
    public static void main(String[] args) throws Exception {
        String propulsionConfigPath = "c:\\Users\\Admin\\Documents\\New folder\\create-propulsion-simulated\\src\\main\\java\\dev\\propulsionteam\\propulsionsimulated\\PropulsionConfig.java";
        String thrusterConfigPath = "c:\\Users\\Admin\\Documents\\New folder\\create_propulsion_simulated\\src\\main\\java\\dev\\createpropulsionsimulated\\config\\ThrusterConfig.java";

        String pConfig = new String(Files.readAllBytes(Paths.get(propulsionConfigPath)));
        String tConfig = new String(Files.readAllBytes(Paths.get(thrusterConfigPath)));

        // Extract fields
        Matcher fieldMatcher = Pattern.compile("public static final ModConfigSpec\\.[a-zA-Z]+Value.*?([^;]+;)", Pattern.DOTALL).matcher(tConfig);
        StringBuilder newFields = new StringBuilder();
        while (fieldMatcher.find()) {
            newFields.append("    ").append(fieldMatcher.group(0)).append("\n");
        }

        // Add java.util.List import if not exists
        if (!pConfig.contains("import java.util.List;")) {
            pConfig = pConfig.replaceFirst("import net.neoforged.neoforge.common.ModConfigSpec;", "import net.neoforged.neoforge.common.ModConfigSpec;\nimport java.util.List;");
        }

        // Extract static init block content from ThrusterConfig
        Matcher staticBlockMatcher = Pattern.compile("static \\{\\s+final ModConfigSpec\\.Builder builder = new ModConfigSpec\\.Builder\\(\\);(.*?)(?:SPEC = builder\\.build\\(\\);|builder\\.pop\\(\\);\\s+builder\\.pop\\(\\);\\s+SPEC = builder\\.build\\(\\);)\\s+\\}", Pattern.DOTALL).matcher(tConfig);
        String staticBlockContent = "";
        if (staticBlockMatcher.find()) {
            staticBlockContent = staticBlockMatcher.group(1);
            // Replace 'builder' with 'SERVER_BUILDER'
            staticBlockContent = staticBlockContent.replace("builder", "SERVER_BUILDER");
        }

        // Also extract defaultFuelProperties method
        Matcher defaultFuelMatcher = Pattern.compile("private static List<String> defaultFuelProperties\\(\\).*?\\}", Pattern.DOTALL).matcher(tConfig);
        String defaultFuelMethod = "";
        if (defaultFuelMatcher.find()) {
            defaultFuelMethod = "    " + defaultFuelMatcher.group(0).replace("ThrusterConfig", "PropulsionConfig") + "\n";
        }
        
        // Inject fields into PropulsionConfig
        pConfig = pConfig.replaceFirst("//Thruster", "//Thruster\\n" + newFields.toString());

        // Inject into static block in PropulsionConfig
        // Find end of SERVER_BUILDER thruster block
        pConfig = pConfig.replaceFirst("SERVER_BUILDER\\.pop\\(\\);\\s+SERVER_BUILDER\\.push\\(\\\"Creative Thruster\\\"\\);", 
            "SERVER_BUILDER.pop();\n" + staticBlockContent + "\n        SERVER_BUILDER.push(\"Creative Thruster\");");
            
        // Inject defaultFuelMethod at the end
        pConfig = pConfig.replaceFirst("\\}\\s*$", defaultFuelMethod + "}\n");
        
        Files.write(Paths.get(propulsionConfigPath), pConfig.getBytes());
    }
}
