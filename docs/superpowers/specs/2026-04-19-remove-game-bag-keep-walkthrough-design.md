# Remove Game Bag, Keep Walkthrough Only Design

## Background

The current product exposes two chat agents:

- `game-bag`
- `game-walkthrough`

The requested change is to remove `game-bag` and keep only the game walkthrough capability.

Today, that dual-agent setup exists in both the frontend and backend:

- The frontend exposes agent selection and defaults to `game-bag`.
- The backend registers both agents and routes requests by the `agent` field.

The goal is to make the product single-purpose: walkthrough and guide assistance only.

## Goals

- Remove `game-bag` from the user-facing product.
- Make `game-walkthrough` the only supported agent.
- Remove the frontend agent switcher and related multi-agent branching.
- Remove the backend `GameBagAgent` implementation and keep routing behavior coherent.
- Keep the existing chat API shape unchanged.

## Non-Goals

- No API contract redesign.
- No attempt to preserve backward compatibility for `game-bag` requests.
- No prompt or RAG behavior change for `game-walkthrough`.
- No unrelated UI refactoring.
- No automated tests in this task. Manual acceptance will be performed by the user.

## Proposed Approach

Use a full single-agent reduction instead of hiding `game-bag` only in the UI.

This means:

- Frontend types, defaults, and visible options are reduced to `game-walkthrough` only.
- The chat header no longer exposes agent switching.
- Backend registration removes `GameBagAgent`.
- Backend routing keeps the same request field but supports only the remaining agent.

This is preferred over a compatibility shim because it keeps the codebase aligned with the actual product scope instead of leaving dead semantic branches behind.

## Frontend Changes

### Agent Model

Reduce the frontend `ChatAgent` type to:

- `game-walkthrough`

Keep the `agent` field in request payloads so the frontend request contract remains stable, but always resolve it to `game-walkthrough`.

### Request Defaults

Update the chat API helper so:

- the default agent is `game-walkthrough`
- `getDefaultChatAgent()` always returns `game-walkthrough`
- the exported agent options no longer include `game-bag`

### View and Header

Update the chat page so it no longer needs agent-selection state beyond the fixed walkthrough value.

Update the header component so it behaves as a walkthrough-only header:

- no agent selector
- existing regenerate action preserved
- no visible reference to `game-bag`

The UI change should be minimal and follow existing layout/style patterns.

## Backend Changes

### Agent Registration

Delete `GameBagAgent` entirely.

Keep `GameWalkthroughAgent` as the only registered `GameMasterAgent`.

### Routing Behavior

Keep `GameMasterAgentRouter` structure unchanged unless simplification is directly required by the removal.

Expected behavior after the change:

- blank or missing `agent` resolves to the only registered default agent
- `game-walkthrough` continues to work
- `game-bag` is rejected as unsupported

This keeps behavior explicit and avoids silently remapping deprecated product concepts.

### Request Payload

Keep `ChatRequestPayload.agent` unchanged to avoid unnecessary API churn.

## Manual Verification

Automated tests are intentionally out of scope for this task at the user's request.

Manual verification focus:

- The frontend no longer shows any `game-bag` or agent-switching UI.
- Sending a chat request still works from the walkthrough interface.
- Regenerate still works.
- Requests with no explicit `agent` still resolve successfully.
- Requests with `agent=game-bag` fail as unsupported.

## Risks and Constraints

- Removing the selector requires care to avoid leaving stale props or state in the chat view/header boundary.
- Old clients that still send `game-bag` will now receive an unsupported-agent error.
- The change should remain surgical: only code directly related to multi-agent behavior should be touched.

## Implementation Summary

1. Remove `game-bag` from frontend types, defaults, and visible options.
2. Remove agent-switch UI wiring from the chat page/header.
3. Delete backend `GameBagAgent`.
4. Keep `GameWalkthroughAgent` as the sole supported agent and preserve existing routing contract.
5. Perform manual acceptance only.
