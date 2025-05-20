import { getContextData } from "@quickjs4j/ContextTestScript_Builtins.mjs";

export function getActualTestData(): string {
    return "context-test-data::" + getContextData();
}
