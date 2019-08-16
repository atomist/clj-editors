/**
 * clojure-sdm editors
 */
export declare function setVersion( f: string, version: string): Promise<any>
export declare function getVersion( f: string): string
export declare function getName( f: string): string
export declare function projectDeps( f: string): void
export declare function cljfmt(f: string): Promise<any>
export declare function updateProjectDep( f: string, libname: string, version: string): void
export declare function rmProjectDep( content: string, libname: string): string
export declare function vault( key: string, f: string): Map<string,string>
export declare function hasLeinPlugin( f: string, symbol: string): boolean

// -----------------------------------------------------------------------

export declare interface Fingerprint { type: string, name: string, sha: string, data: any, version?: string, abbreviation?: string }
export declare interface FromTo { from: any[], to: any[] }
export declare interface DiffData { from?: Fingerprint, to: Fingerprint, data: FromTo, owner: string, repo: string, sha: string, providerId: string, channel: string, branch: string }

// -------------------------------------------------------------------------

export declare interface Decision {name?: string, decision?: string}
export declare function voteResults<X extends Decision>(votes: X[]): { failed: boolean; failedVotes: X[]; };

export declare function checkFingerprintTargets(
    queryPreferences: () => Promise<any>,
    sendMessage: (s: string, targetFP: Fingerprint, fingerprint: Fingerprint) => Promise<any>,
    inSync: (fingerprint: Fingerprint) => Promise<any>,
    diff: DiffData
): Promise<any>

export declare function broadcastFingerprint(
    queryFingerprints: (type: string, name: string) => Promise<any>,
    fingerprint: { name: string, type: string, sha: string },
    callback: (owner: string, repo: string, channel: string) => Promise<any>
): Promise<any>

export declare function partitionByFeature(
    fps: Fingerprint[],
    callback: (partitioned: {type: string, additions: {name: string, sha: string, data: string}[]}[]) => Promise<any>
): Promise<any>

/**
 * Clojure fingerprint computations and editors
 */
export declare function mavenDeps(f1: string): Promise<Fingerprint[]>
export declare function mavenCoordinates(f1: string): Promise<Fingerprint[]>
export declare function leinDeps(f1: string): Promise<Fingerprint[]>
export declare function leinCoordinates(f1: string): Promise<Fingerprint[]>
export declare function logbackFingerprints(f1: string): Promise<Fingerprint[]>
export declare function cljFunctionFingerprints(f1: string): Promise<Fingerprint[]>

export declare function applyFingerprint(f1: string, fp: Fingerprint): Promise<any>

/**
 * Utility functions to rewrite in typescript
 */
export declare function renderDiff(diff: DiffData): string
export declare function renderOptions(options: { text: string, value: string }[]): string
export declare function renderData(x: any): string
export declare function commaSeparatedList(x: any): string
export declare function sha256(data: string): string
export declare function consistentHash(data: any): string
export declare function renderProjectLibDiff(diff: DiffData, target: Fingerprint): { title: "string", description: string }
