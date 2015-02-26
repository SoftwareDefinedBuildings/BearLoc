@0xa39027cb8d9c2f80;

interface Algorithm {
  # BearLoc Algorithm Interface

  localize @0 () -> (location: Location);
  # localize based on input

  struct Location {
    country @0 :Text;
    state @1 :Text;
    city @2 :Text;
    street @3 :Text;
    building @4 :Text;
    locale @5 :Text;
  }
}
