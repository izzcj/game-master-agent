# Embedding Model Chunking Design

## Background

The current knowledge base ingestion flow uses `TokenTextSplitter` with fixed chunk parameters:

- `chunk-size`
- `min-chunk-size-chars`
- `min-chunk-length-to-embed`
- `max-num-chunks`

This is simple, but it ignores Markdown structure and often produces chunks that are either too fragmented or cut across meaningful walkthrough sections.

The requested change is to replace fixed token-based chunking with semantic chunking driven by `EmbeddingModel`.

## Goals

- Replace `TokenTextSplitter` with a Markdown-aware semantic chunker.
- Preserve Markdown structure before applying semantic merge decisions.
- Use `EmbeddingModel` to decide whether adjacent fragments belong in the same final chunk.
- Replace the old chunking configuration with new semantic chunking configuration.
- Keep the ingestion pipeline shape unchanged: `load -> chunk -> vectorStore.add`.

## Non-Goals

- No retrieval-side behavior change beyond better chunk quality.
- No changes to Markdown loading behavior.
- No automatic tests in this task.
- No extra features such as cross-document merge, chunk summarization, or hybrid ranking changes.

## Proposed Architecture

Introduce a new chunking component, `SemanticMarkdownChunker`, responsible only for turning loaded Markdown documents into final vector-store chunks.

### Responsibilities

- Parse Markdown text into structure-first base fragments.
- Keep section context through heading hierarchy.
- Use `EmbeddingModel` to compare adjacent fragments.
- Merge adjacent fragments when they are semantically close and still within configured size limits.
- Produce final `Document` chunks with enriched metadata.

### Wiring Changes

- `GameMasterRagAutoConfiguration`
  - Remove the `TokenTextSplitter` bean.
  - Inject `EmbeddingModel`.
  - Register `SemanticMarkdownChunker`.
- `KnowledgeBaseIngestionService`
  - Replace `TokenTextSplitter` dependency with `SemanticMarkdownChunker`.
  - Keep ingestion flow unchanged.
- `MarkdownKnowledgeBaseDocumentLoader`
  - No change.

## Configuration Changes

Remove these properties:

- `gamemaster.rag.chunk-size`
- `gamemaster.rag.min-chunk-size-chars`
- `gamemaster.rag.min-chunk-length-to-embed`
- `gamemaster.rag.max-num-chunks`

Add semantic chunking properties:

- `gamemaster.rag.semantic.max-chunk-chars`
  - Hard cap for a final merged chunk.
- `gamemaster.rag.semantic.min-chunk-chars`
  - Fragments shorter than this should attempt semantic merge with following fragments.
- `gamemaster.rag.semantic.similarity-threshold`
  - Minimum cosine similarity required to merge adjacent fragments.
- `gamemaster.rag.semantic.max-merge-lookahead`
  - Maximum number of following fragments considered during merge.
- `gamemaster.rag.semantic.preserve-headings`
  - Whether final chunk content should include heading prefixes.

Default values should be conservative and biased toward structure preservation over aggressive merge.

## Chunking Rules

The chunker will run in two phases.

### Phase 1: Structure-First Fragment Extraction

Split each Markdown document into base fragments using Markdown structure:

- Headings open a new section context and update the current heading path.
- Paragraphs are split by blank lines.
- Consecutive list items are grouped into one fragment.
- Code blocks remain intact as one fragment.
- Quote blocks remain intact as one fragment.
- Tables remain intact as one fragment.
- Each fragment is tagged with a structure type.
- If `preserve-headings=true`, fragment text includes the current heading path as contextual prefix.

This phase produces the minimum meaningful units that are safe to merge later.

### Phase 2: Adjacent Semantic Merge

Within the same section, merge only adjacent base fragments:

- If the current fragment is shorter than `min-chunk-chars`, try merging forward.
- Compare the current accumulated content and the next fragment using `EmbeddingModel`.
- Merge only when:
  - similarity is greater than or equal to `similarity-threshold`
  - merged length does not exceed `max-chunk-chars`
  - lookahead count does not exceed `max-merge-lookahead`
- Do not merge across heading boundaries by default.
- Stop merging as soon as one merge condition fails.

This keeps structure deterministic and limits semantic decisions to local adjacency.

## Metadata

Each produced chunk should retain or add these metadata fields:

- `source`
- `fileName`
- `sectionPath`
- `chunkIndex`
- `structureType`
- `semanticMerged`

`sectionPath` is the full heading chain for the chunk, for example `Stormveil Castle > Main Gate vs Side Path`.

## Manual Verification

This task does not include automated tests.

Manual verification focus:

- Application starts and knowledge base ingestion still runs.
- Vector store receives chunks successfully.
- Chunk count is lower than raw paragraph splitting, but chunks do not grow uncontrollably.
- Retrieval results include more complete section context than before.

## Risks and Constraints

- Embedding calls during chunking increase ingestion cost and latency.
- Markdown parsing should stay simple and scoped to the structures present in the repository.
- Over-aggressive similarity defaults could collapse unrelated adjacent fragments, so defaults must be conservative.
- Under-aggressive similarity defaults could leave many small fragments, which is acceptable as the safer failure mode.

## Implementation Summary

1. Add semantic chunking configuration to `GameMasterRagProperties`.
2. Add `SemanticMarkdownChunker`.
3. Replace `TokenTextSplitter` wiring in autoconfiguration and ingestion service.
4. Update `application.yaml` to remove old chunking properties and add semantic properties.
5. Compile and perform manual verification only.
