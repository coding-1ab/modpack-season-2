var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI')

// Necessary to fix forge being a terrible loader
function initializeCoreMod() {
    return {
        "registrycoremod": {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.core.registries.BuiltInRegistries',
                'methodName': '<clinit>',
                'methodDesc': '()V'
            },
            'transformer': function (method) {
                var CreateBuiltInRegistries = "com/simibubi/create/api/registry/CreateBuiltInRegistries";

                var insn = ASMAPI.buildMethodCall(CreateBuiltInRegistries, "init", "()V", ASMAPI.MethodType.STATIC)

                method.instructions.insertBefore(method.instructions.getLast(), insn)

                return method;
            }
        }
    }
}
