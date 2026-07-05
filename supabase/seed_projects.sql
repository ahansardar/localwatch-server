begin;

-- Portfolio slugs are stable identifiers. The partial index allows existing
-- rows that still use the schema's empty-string default while enforcing
-- uniqueness for real project slugs.
create unique index if not exists projects_slug_unique
  on public.projects (slug)
  where slug is not null and slug <> '';

insert into public.projects (
  title,
  tagline,
  problem_statement,
  features,
  screenshots,
  role,
  challenges,
  outcomes,
  links,
  optional_additions,
  slug,
  tech_stack,
  date,
  thumbnail,
  video_url,
  status,
  versions
)
values (
  'LocalWatch',
  'Your phone is the server. Your Wi-Fi is the cinema.',
  'Sharing a personal video library with nearby devices usually requires cloud uploads, accounts, internet access, or a dedicated media server. LocalWatch turns an Android phone into a private, offline-first video server that any device on the same local network can use through a browser.',
  array[
    'Select a video folder through Android''s secure system folder picker',
    'Start a local HTTP server and share access by QR code, URL, or Android share sheet',
    'Browse a responsive video library with search, sorting, and grid or list layouts',
    'Stream videos with HTTP byte-range requests and seek support',
    'Protect the library with an optional PIN',
    'Allow or disable file downloads from the host app',
    'Rescan the selected folder and inspect recent clients',
    'Check GitHub Releases for updates and verify downloads with SHA-256'
  ]::text[],
  array[
    'https://raw.githubusercontent.com/ahansardar/localwatch-server/main/docs/screenshots/android-dashboard-running.png',
    'https://raw.githubusercontent.com/ahansardar/localwatch-server/main/docs/screenshots/android-library.png',
    'https://raw.githubusercontent.com/ahansardar/localwatch-server/main/docs/screenshots/web-library-desktop.png'
  ]::text[],
  'Sole developer — product design, Android development, embedded web UI, local networking, media streaming, testing, release engineering, and documentation.',
  jsonb_build_object(
    'network_streaming', 'Implemented correct HTTP Range, 206 Partial Content, Content-Range, and bounded streaming so browser playback can seek without loading an entire file.',
    'privacy', 'Used Android''s Storage Access Framework, persisted read-only permissions, and opaque media IDs so filesystem paths are never exposed to viewers.',
    'offline_viewer', 'Embedded the complete HTML, CSS, JavaScript, icons, and player experience inside the Android app so viewers need no internet connection.',
    'android_lifecycle', 'Kept the server reliable through a foreground service, configurable keep-awake behavior, and clear start and stop controls.',
    'updates', 'Built a GitHub Releases update flow with Markdown release notes, visible progress, optional SHA-256 verification, and Android installer handoff.'
  ),
  array[
    'Released a complete open-source v1.0.0 Android application',
    'Enabled private, account-free media sharing without cloud storage or analytics',
    'Supported phones, tablets, laptops, and TVs through a browser-first viewer',
    'Delivered seekable local streaming, optional downloads, PIN access, and host controls',
    'Established automated Android build verification and a signed release workflow'
  ]::text[],
  jsonb_build_object(
    'repository', 'https://github.com/ahansardar/localwatch-server',
    'release', 'https://github.com/ahansardar/localwatch-server/releases/latest',
    'issues', 'https://github.com/ahansardar/localwatch-server/issues',
    'license', 'https://github.com/ahansardar/localwatch-server/blob/main/LICENSE'
  ),
  array[
    'Open source under the MIT License',
    'No accounts, advertising, analytics, or cloud media storage',
    'Android 8.0 and newer support',
    'Browser playback with VLC and download fallbacks for unsupported codecs'
  ]::text[],
  'localwatch',
  array[
    'Kotlin',
    'Jetpack Compose',
    'Material 3',
    'NanoHTTPD',
    'Android Storage Access Framework',
    'DocumentFile',
    'HTML',
    'CSS',
    'JavaScript',
    'WorkManager',
    'GitHub Releases API',
    'JUnit',
    'Gradle'
  ]::text[],
  date '2026-07-05',
  'https://raw.githubusercontent.com/ahansardar/localwatch-server/main/app/src/main/res/drawable-nodpi/localwatch_logo.png',
  null,
  'Released',
  json_build_array(
    json_build_object(
      'version', '1.0.0',
      'date', '2026-07-05',
      'status', 'stable',
      'release_url', 'https://github.com/ahansardar/localwatch-server/releases/tag/v1.0.0',
      'highlights', json_build_array(
        'Native Android host application',
        'Responsive offline browser viewer',
        'Seekable byte-range video streaming',
        'PIN protection and download controls',
        'GitHub-powered application updates'
      )
    )
  )
)
on conflict (slug) where slug is not null and slug <> ''
do update set
  title = excluded.title,
  tagline = excluded.tagline,
  problem_statement = excluded.problem_statement,
  features = excluded.features,
  screenshots = excluded.screenshots,
  role = excluded.role,
  challenges = excluded.challenges,
  outcomes = excluded.outcomes,
  links = excluded.links,
  optional_additions = excluded.optional_additions,
  tech_stack = excluded.tech_stack,
  date = excluded.date,
  thumbnail = excluded.thumbnail,
  video_url = excluded.video_url,
  status = excluded.status,
  versions = excluded.versions;

commit;
