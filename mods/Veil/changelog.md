- Remove ImGui as a dependency. Instead, [ImGuiMC](https://modrinth.com/mod/imguimc) can optionally be installed for
  Veil editors.
- When ImGui is not installed the editor key is removed
- Remove deprecated features
- Move example resourcepacks to the [example mod](https://github.com/FoundryMC/veil-example-mod)
- Remove unused legacy shaders
- When vanilla shaders fail to locate an import, the shader is now skipped. This prevents the shader from being placed
  in an invalid state when Veil is installed