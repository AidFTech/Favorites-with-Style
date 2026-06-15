# Style Guide

Favorites with Style uses Yamaha SFF style files for accompaniment. These files consist of all sections of the style in standard MIDI format plus extra bytes at the end known as the CASM. The CASM defines all playback rules associated with the style including high notes, upper and lower limits, and note maps.

A style can use any of the sixteen available MIDI channels, however, they must map to channels 9 and up. Typical definitions for the output channels are as follows:

- Channel 9: Sub-Rhythm (percussion)

- Channel 10: Rhythm (percussion)

- Channel 11: Bass

- Channel 12: Chord 1 (main chord)

- Channel 13: Chord 2 (main chord)

- Channel 14: Pad (typically continuous chord played through each measure)

- Channel 15: Phrase 1 (melodic phrase)

- Channel 16: Phrase 2 (melodic phrase)

Behavior and mapping for each channel are dictated by the rules defined in the CASM binary. For each “input” source channel used in the style MIDI, in each section of the style, the following rules can be defined:

- Destination Channel: The “output” channel events should map to. This can be the same as the source channel, or, for example, in the case of non-linear phrase behavior, any of the eight reserved style channels.

- Source Root and Chord: The accompaniment root and chord defined in the style MIDI. Ideally C M7.

- Note Transposition Rule: Defines how to transpose the accompaniment based on the set root. Two options are available, Note Fixed and Note Transposition. Note Transposition simply transposes each note a fixed number of steps depending on the destination root. Root fixed behavior is defined in the table below.

- Note Transposition Table: Defines how to transpose the accompaniment based on chord. Set to “Bypass” to leave each note as it is after root transposition. Other behaviors are defined in the table below. *Note: An NTT mode of “Bypass” combined with a transposition rule of Note Fixed will not transpose the notes at all, regardless of chord or root. This combination should be used for percussion tracks.*

- High Key: For the Root Transposition mode, the highest root the accompaniment will be transposed to before moving down an octave.

- High/Low Limits: The highest and lowest physical notes that can be played in this section. If a note above the upper limit or below the lower limit is mapped, it is transposed down or up an octave.

- Retrigger Rule: Defines whether to hold unchanging notes if the chord changes mid-note.

- Note Play: Defines the root notes for which this (input) channel will play.

- Chord Play: Defines the chords for which this (input) channel will play. Use for non-linear phrase behavior or extra notes for certain chords.

# Sections

A style can be divided into groups of events, or sections, for different parts of a song. Each section loops with the defined chord until a section change event is triggered. For older keyboards such as basic Casio A², each style typically has a looping section and a fill-in section. Newer Casio keyboards typically have the following sections

- Intro

- Normal

- Normal Fill In

- Variation

- Variation Fill In

- Ending

Yamaha PSR-E keyboards typically have the same structure with slightly different behavior and nomenclature:

- Intro

- Main A

- Fill In AB

- Main B

- Fill In BA

- Ending

Higher-end Yamaha keyboards can have up to four sections labeled A-D with a fill-in for each.

A fill-in can either replace just the percussion track (typical for PSR-E) or all tracks of the style (typical for Casio or four-section PSR). Favorites with Style accepts both formats. If chromatic (transposing, non-percussion) events are defined during a fill-in, they are played when that fill-in is triggered. Otherwise, the chromatic events from the previous section are used.

## CSEG and Sections

CASM binary defines groups of rules known as CSEGs. Each CSEG can apply to one or more sections of the style. Typically, the intro and ending, if melodic, will have their own CSEG.

# NTR Root Fixed Map

The following table defines how notes are transposed by key if the Note Transposition Rule is set to Root Fixed. This rule is recommended for the chord NTT mode (or bypass for percussion) only.

Note that most root notes will transpose B and C together, assuming a M7 chord. Defining a C in a Root Fixed channel is not recommended. To play a root note in the Root Fixed rule, define it in another channel with the Root Transposition rule.

| **Root Note** | **Map** |
| :-: | :-: |
| C | C→C, E→E, G→G, B→B |
| C♯ | C→C♯, E→F, G→A♭, B→C |
| D | C→D, E→F♯, G→A, B→C♯ |
| E♭ | C→B♭, E→D, G→G, B→B♭ |
| E | C→B, E→E♭, G→A♭, B→B |
| F | C→C, E→E, G→A, B→C |
| F♯ | C→C♯, E→F, G→B♭, B→C♯ |
| G | C→B, E→D, G→F♯, B→B |
| A♭ | C→C, E→E♭, G→G, B→C |
| A | C→C♯, E→E, G→A♭, B→C♯ |
| B♭ | C→D, E→F, G→A, B→D |
| B | C→B, E→E♭, G→F♯, B→B♭ |


# NTT Map

Notes defined in a style are transposed as follows in chord and melody/bass mode. Melody and bass have the same mapping, however, if a bass chord is defined, the bass map follows that chord instead of the main chord.

The following mappings assume a C M7 reference chord. A note followed by +8 indicates that the note is transposed to the next octave up. E.g. a note defined as B4 will transpose up to C5.

Notes defined in the accompaniment MIDI that do not match the reference chord and root are not played.

| **Chord** | **Chord Mode Mapping** | **Melody/Bass Mapping** |
| :-: | :-: | :-: |
| (major) | C→C, E→E, G→G, B→C+8 | C→C, D→D, E→E, G→G, A→A, B→C+8 |
| 6 | C→C, E→E, G→A, B→C+8 | C→C, D→D, E→E, G→G, A→A, B→C+8 |
| M7 | C→C, E→E, G→G, B→B | C→C, D→D, E→E, G→G, A→A, B→B |
| M7(♯11) | C→C, E→E, G→F♯, B→B | C→C, D→D, E→E, G→G, A→A, B→B |
| (9) | C→C, E→E, G→G, B→D+8 | C→C, D→D, E→E, G→G, A→A, B→C+8 |
| M7(9) | C→C, E→E, G→B, B→D+8 | C→C, D→D, E→E, G→G, A→A, B→B |
| 6(9) | C→C, E→E, G→A, B→D+8 | C→C, D→D, E→E, G→G, A→A, B→C+8 |
| aug | C→C, E→E, G→A♭, B→C+8 | C→C, D→D, E→E, G→A♭, A→B♭, B→C+8 |
| m | C→C, E→E♭, G→G, B→C+8 | C→C, D→D, E→E♭, G→G, A→G, B→C+8 |
| m6 | C→C, E→E♭, G→A, B→C+8 | C→C, D→D, E→E♭, G→A, A→A, B→C+8 |
| m7 | C→C, E→E♭, G→G, B→B♭ | C→C, D→C, E→E♭, G→G, A→B♭, B→B♭ |
| m7♭5 | C→C, E→E♭, G→F♯, B→B♭ | C→C, D→C♯, E→E♭, G→F♯, A→A♭, B→B♭ |
| m(9) | C→C, E→E♭, G→G, B→D+8 | C→C, D→D, E→E♭, G→G, A→G, B→C+8 |
| m7(9) | C→C, E→E♭, G→B♭, B→D+8 | C→C, D→D, E→E♭, G→G, A→B♭, B→B♭ |
| m7(11) | C→C, E→E♭, G→F, B→B♭ | C→C, D→D, E→E♭, G→G, A→B♭, B→B♭ |
| mM7 | C→C, E→E♭, G→G, B→B | C→C, D→D, E→E♭, G→G, A→B♭, B→B |
| mM7(9) | C→C, E→E♭, G→B, B→D+8 | C→C, D→D, E→E♭, G→G, A→B♭, B→B |
| dim | C→C, E→E♭, G→F♯, B→C+8 | C→C, D→D, E→E♭, G→F♯, A→A, B→C+8 |
| dim7 | C→C, E→E♭, G→F♯, B→A | C→C, D→C, E→E♭, G→F♯, A→A, B→C+8 |
| 7 | C→C, E→E, G→G, B→B♭ | C→C, D→C, E→E, G→G, A→B♭, B→B♭ |
| 7sus | C→C, E→F, G→G, B→B♭ | C→C, D→C, E→F, G→G, A→B♭, B→B♭ |
| 7♭5 | C→C, E→E, G→F♯, B→B♭ | C→C, D→D, E→E, G→F♯, A→B♭, B→B♭ |
| 7(9) | C→C, E→E, G→B♭, B→D+8 | C→C, D→D, E→E, G→G, A→B♭, B→B♭ |
| 7(♯11) | C→C, E→E, G→F♯, B→B♭ | C→C, D→D, E→E, G→G, A→A, B→B♭ |
| 7(13) | C→C, E→E, G→A, B→B♭ | C→C, D→D, E→E, G→G, A→A, B→B♭ |
| 7(♭9) | C→C, E→E, G→B♭, B→C♯+8 | C→C, D→C♯, E→E, G→G, A→B♭, B→B♭ |
| 7(♭13) | C→C, E→E, G→A♭, B→B♭ | C→C, D→C♯, E→E, G→G, A→A♭, B→B♭ |
| 7(♯9) | C→C, E→E, G→B♭, B→E♭+8 | C→C, D→E♭, E→E, G→G, A→B♭, B→B♭ |
| M7aug | C→C, E→E, G→A♭, B→B | C→C, D→D, E→E, G→A♭, A→A, B→B |
| 7aug | C→C, E→E, G→A♭, B→B♭ | C→C, D→D, E→E, G→A♭, A→A♭, B→B |
| (1+8) | C→C, E→C, G→C+8 B→C+8 | C→C, D→C, E→C, G→C+8, A→C+8, B→C+8 |
| (1+5) | C→C, E→C, G→G B→C+8 | C→C, D→C, E→C, G→G, A→G, B→C+8 |
| sus4 | C→C, E→F, G→G B→C+8 | C→C, D→D, E→F, G→G, A→G, B→C+8 |
| sus2 | C→C, E→D, G→G B→C+8 | C→C, D→D, E→D, G→G, A→A, B→C+8 |
| M7♭5 | C→C, E→E, G→F♯, B→B | C→C, D→D, E→E, G→F♯, A→A, B→B |
| ♭5 | C→C, E→E, G→F♯, B→C+8 | C→C, D→D, E→E, G→F♯, A→F♯, B→C+8 |
| mM7♭5 | C→C, E→E♭, G→F♯, B→B | C→C, D→C♯, E→E♭, G→F♯, A→A, B→B |


