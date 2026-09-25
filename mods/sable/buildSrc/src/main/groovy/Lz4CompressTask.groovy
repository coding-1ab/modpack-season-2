import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import net.jpountz.lz4.LZ4FrameOutputStream
import java.nio.file.Files

abstract class Lz4CompressTask extends DefaultTask {

    @InputFile
    abstract RegularFileProperty getInputJar()

    @OutputFile
    abstract RegularFileProperty getOutputJar()

    @TaskAction
    void compress() {
        def inputFile = inputJar.get().asFile
        def outputFile = outputJar.get().asFile

        byte[] bytes = Files.readAllBytes(inputFile.toPath())

        outputFile.parentFile.mkdirs()

        new FileOutputStream(outputFile).withCloseable { fos ->
            new LZ4FrameOutputStream(
                    fos,
                    LZ4FrameOutputStream.BLOCKSIZE.SIZE_4MB,
                    bytes.length,
                    LZ4FrameOutputStream.FLG.Bits.BLOCK_INDEPENDENCE
            ).withCloseable { lz4 ->
                lz4.write(bytes)
            }
        }
    }
}
