/**
 * clojure-sdm editors
 */
export declare function setVersion( f: string, version: string): Promise<any>
export declare function getVersion( f: string): string
export declare function getName( f: string): string
export declare function projectDeps( f: string): void
export declare function cljfmt(f: string): Promise<any>
export declare function updateProjectDep( f: string, libname: string, version: string): void
export declare function vault( key: string, f: string): Map<string,string>
export declare function hasLeinPlugin( f: string, symbol: string): boolean
