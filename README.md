# Favorites: with Style

*Note: This application is still very much in the prototype phase.*

Favorites with Style, or FWS for short, is a Java-based GUI MIDI arranger designed specifically for songs with auto accompaniment styles, like those included on keyboards made by Casio and Yamaha.

For Casio users, “Voice” = Tone and “Style” = Rhythm.

<img width="1920" height="1170" alt="image" src="https://github.com/user-attachments/assets/38a2e0b3-4ad0-48ec-b0f8-c2291ea0170d" />

The defining feature of Favorites with Style is the “Style” part. Having to manually define accompaniment in a MIDI song can be a time-consuming process and error-prone if you want the accompaniment to emulate a style on your keyboard. Favorites with Style allows you to add accompaniment in a few MIDI events. Simply load or define the desired style for your song and use events to define how the style plays and what chords are output. When you play the song, the accompaniment is automatically calculated based on your input, as if you were playing it on your keyboard. There is even a simulated keyboard display for a more authentic experience.

<img width="515" height="215" alt="image" src="https://github.com/user-attachments/assets/e15b92f2-9613-4e36-8357-aab20fdbf947" />

You can also define custom styles to expand the library of your instrument, even if it doesn’t support custom styles on its own. The style editor can play back any section of a style in a loop and automatically changes the output based on chord input from your keyboard.

FWS supports styles in the Yamaha SFF1 format. This is a standard MIDI file with an extra set of bytes at the end. These bytes are known as the CASM and are used to define rules for the style, such as the highest key to play a section part in, how to retrigger notes if the chord changes, whether to play certain parts only for certain chords, and more. CASM data can be defined and added to a standard MIDI file in FWS.

Currently, the FWS arranger/editor itself is in development. Eventually, I plan to create a second program for automated multi-step lessons, similar to those on Yamaha and Casio keyboards. If you are better at dragging and dropping than playing the keyboard, you can create a song you want to learn and step through the lessons at your own pace. The “Common” folder contains Java and Rust code applicable to both programs.

## Features

### Active

Currently, the following features are available in Favorites with Style:

- MIDI Song Arrangement and Playback: Create MIDI songs and sequences with an intuitive drag-and-drop interface and play them on any MIDI keyboard.

  - Songs can be played in real-time with the program through your keyboard or exported to SMF for easy sharing.

  - Section and chord events can be defined for easy auto accompaniment.

  - Transport buttons and a playhead make the song easy to navigate.

- Simulated Keyboard Display: A mock display shows the active melody and chord notes and chord name if applicable.

- Accompaniment Styles: Create, play, and incorporate accompaniment styles for FWS songs or performance playback on your keyboard.

  - Styles are created in the Yamaha SFF1 format, which can be easily derived from any MIDI file or created with a compatible instrument. CASM rules (high keys, limits, retrigger rules, etc) can be defined in the program or externally.

  - Song accompaniment is calculated from the style based on section and chord events and played automatically in the song.

  - Styles can be played live in the style editor with chord input from your keyboard.

- MIDI Import and Export

  - Channel Swapping: By default, most Yamaha keyboards display events from channel 1 (RH) and 2 (LH) in loaded MIDI songs, while most Casio keyboards display channels 3 (LH) and 4 (RH). Channels can be swapped in the exported MIDI file to ensure the correct notes are displayed.

  - Yamaha Chord Display: Chords can be displayed on any compatible Yamaha instrument.

### To Do/Planned

- Accelerator Functions such as Cut/Copy/Paste

- Instrument Profiles: Every keyboard has a different set of available voices, features, system exclusive events, title handling modes, among other things. Instrument profiles will define voice lists, how chord and fingering display is triggered (if supported), and any other model-specific MIDI events to get the most out of an instrument in Favorites with Style.

  - Casio Chord/Fingering Display: Currently, I only have the sysex syntax for chord name display on Yamaha PSR and PSR-E keyboards. It is possible to display this information on Casio instruments as well, as shown in [these](https://www.youtube.com/watch?v=Sn-iFQpB4ek) [videos](https://www.youtube.com/watch?v=rXEI-dPivvE).

  - Yamaha Live Chord Display: The sysex syntax I have for Yamaha chord names only seems to work for loaded songs, not streamed playback to the instrument. This may work for higher-end instruments but I have not tried it.

- Voice Substitutions: When playing a song written for one type of keyboard on another, certain voices (particularly drum kits) may need to be redefined for the new instrument. This substitution will be performed automatically during play or export (not changed in the original file) to ensure full compatibility with the target instrument.

  - Note Substitutions: For older keyboards, notably Casio’s basic A2 keyboards; the percussion map has claves where the vibraslap would normally be. The percussion map before the bass drum is also different between Casio and Yamaha instruments. Note substitutions would automatically play the correct percussion sound when playing songs between instruments.

- Looped Style Recording: MIDI events can be recorded from an instrument, but the recording does not loop. While this is ideal for importing/copying a style from an instrument or recording a song, section looping will have to be implemented to record a custom style by playing the keyboard.

- Playback During Recording: Most MIDI programs will play existing events during recording, this has yet to be implemented here.

  - Chord Recording: Since the program can respond to chord play from a keyboard, it only makes sense to allow chord events to be recordable this way as well.

- Keyboard Style Play: To change a style section, you must select the desired section from a dropdown in the Style Editor. This is fine for testing but less than ideal for live play. Eventually, these changes can be mapped to buttons on screen (for a hybrid tablet) or your computer keyboard to quickly change to the desired section during live style play.

- Score Display and Export

### Future

- SFF2 File Support

- Built-in Conversion from Casio CKF to Yamaha SFF to import custom Casio styles (rhythms)

- An automated lesson mode designed to emulate the multi-step lessons included in Casio and Yamaha instruments- this will likely be a separate program, hence the “Common” and “Editor” folders in the repository.

