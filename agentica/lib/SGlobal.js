"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.SGlobal = void 0;
const __typia_transform__assertGuard = __importStar(require("typia/lib/internal/_assertGuard.js"));
const dotenv_1 = __importDefault(require("dotenv"));
const dotenv_expand_1 = __importDefault(require("dotenv-expand"));
const tstl_1 = require("tstl");
const typia_1 = __importDefault(require("typia"));
class SGlobal {
    static get env() {
        return environments.get();
    }
}
exports.SGlobal = SGlobal;
const environments = new tstl_1.Singleton(() => {
    const env = dotenv_1.default.config();
    dotenv_expand_1.default.expand(env);
    return (() => { const _io0 = input => (undefined === input.OPENAI_API_KEY || "string" === typeof input.OPENAI_API_KEY) && ("string" === typeof input.PORT && RegExp(/^[+-]?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?$/).test(input.PORT)); const _ao0 = (input, _path, _exceptionable = true) => (undefined === input.OPENAI_API_KEY || "string" === typeof input.OPENAI_API_KEY || __typia_transform__assertGuard._assertGuard(_exceptionable, {
        method: "typia.assert",
        path: _path + ".OPENAI_API_KEY",
        expected: "(string | undefined)",
        value: input.OPENAI_API_KEY
    }, _errorFactory)) && ("string" === typeof input.PORT && RegExp(/^[+-]?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?$/).test(input.PORT) || __typia_transform__assertGuard._assertGuard(_exceptionable, {
        method: "typia.assert",
        path: _path + ".PORT",
        expected: "`${number}`",
        value: input.PORT
    }, _errorFactory)); const __is = input => "object" === typeof input && null !== input && _io0(input); let _errorFactory; return (input, errorFactory) => {
        if (false === __is(input)) {
            _errorFactory = errorFactory;
            ((input, _path, _exceptionable = true) => ("object" === typeof input && null !== input || __typia_transform__assertGuard._assertGuard(true, {
                method: "typia.assert",
                path: _path + "",
                expected: "IEnvironments",
                value: input
            }, _errorFactory)) && _ao0(input, _path + "", true) || __typia_transform__assertGuard._assertGuard(true, {
                method: "typia.assert",
                path: _path + "",
                expected: "IEnvironments",
                value: input
            }, _errorFactory))(input, "$input", true);
        }
        return input;
    }; })()(process.env);
});
//# sourceMappingURL=SGlobal.js.map