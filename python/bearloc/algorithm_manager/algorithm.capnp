@0xa39027cb8d9c2f80;

interface Algorithm {
  # BearLoc Algorithm Interface

  localize @0 () -> (location: Location);
  # localize based on input

  struct Location {
    country @0 :Text;
    building @1 :Text;
    room @2 :Text;
  }
}
