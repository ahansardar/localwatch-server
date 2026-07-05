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
  jsonb_build_array(
    jsonb_build_object(
      'solution', 'Implemented standards-compliant HTTP Range handling with 206 Partial Content responses, Content-Range headers, and bounded input streams.',
      'challenge', 'Supporting smooth browser seeking without loading an entire video into memory'
    ),
    jsonb_build_object(
      'solution', 'Used Android''s Storage Access Framework with persisted read-only permissions and opaque media IDs so viewers never receive filesystem paths.',
      'challenge', 'Sharing user-selected media while respecting Android scoped-storage and privacy rules'
    ),
    jsonb_build_object(
      'solution', 'Embedded the complete HTML, CSS, JavaScript, icons, and player interface in the APK and served every asset from the phone.',
      'challenge', 'Delivering a polished browser experience that works completely without internet access'
    ),
    jsonb_build_object(
      'solution', 'Ran the HTTP server through an Android foreground service with clear lifecycle controls, notifications, configurable ports, and optional keep-awake behavior.',
      'challenge', 'Keeping the local server reliable while Android activities pause, rotate, or move to the background'
    ),
    jsonb_build_object(
      'solution', 'Added container detection, browser-friendly MIME types, download controls, and VLC guidance for media that a browser cannot decode.',
      'challenge', 'Handling inconsistent video codec support across phones, tablets, laptops, and smart TVs'
    ),
    jsonb_build_object(
      'solution', 'Built a GitHub Releases updater with Markdown release notes, visible download progress, optional SHA-256 verification, and Android installer handoff.',
      'challenge', 'Providing trustworthy application updates without an app-store dependency'
    ),
    jsonb_build_object(
      'solution', 'Separated scanning, server, settings, and UI state responsibilities and used Compose state carefully to keep dashboards and library views responsive.',
      'challenge', 'Maintaining a responsive Jetpack Compose interface while scanning storage and serving multiple clients'
    )
  ),
  array[
    'Released a complete open-source v1.0.0 Android application',
    'Enabled private, account-free media sharing without cloud storage or analytics',
    'Supported phones, tablets, laptops, and TVs through a browser-first viewer',
    'Delivered seekable local streaming, optional downloads, PIN access, and host controls',
    'Established automated Android build verification and a signed release workflow'
  ]::text[],
  jsonb_build_object(
    'live', 'https://github.com/ahansardar/localwatch-server/releases',
    'github', 'https://github.com/ahansardar/localwatch-server'
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
      'download_url', 'https://github.com/ahansardar/localwatch-server/releases/tag/v1.0.0',
      'title', 'First Public Release',
      'sections', json_build_array(
        json_build_object(
          'category', 'Android Host & Storage',
          'features', json_build_array(
            'Native Kotlin and Jetpack Compose host application',
            'Secure folder selection through Android''s Storage Access Framework',
            'Foreground local server with start, stop, copy, share, and QR controls',
            'Folder rescanning, recent-client visibility, and configurable keep-awake behavior'
          )
        ),
        json_build_object(
          'category', 'Local Streaming & Playback',
          'features', json_build_array(
            'HTTP byte-range streaming with seek support',
            'Bounded media streaming without loading complete files into memory',
            'Optional original-file downloads controlled by the host',
            'MP4, MKV, WebM, AVI, MOV, and M4V container discovery'
          )
        ),
        json_build_object(
          'category', 'Browser Viewer',
          'features', json_build_array(
            'Responsive browser library for phones, tablets, laptops, and TVs',
            'Video search, sorting, and grid or list layouts',
            'Built-in player with playback-speed, fullscreen, sharing, and download controls',
            'Fully embedded HTML, CSS, JavaScript, icons, and player assets for offline use'
          )
        ),
        json_build_object(
          'category', 'Privacy & Access Controls',
          'features', json_build_array(
            'Optional PIN protection for the shared library',
            'Opaque media identifiers with no exposed filesystem paths',
            'No accounts, analytics, advertising, or cloud media uploads',
            'All viewing and streaming stays on the local Wi-Fi network or hotspot'
          )
        ),
        json_build_object(
          'category', 'Updates & Release Engineering',
          'features', json_build_array(
            'Daily update checks through the GitHub Releases API',
            'Markdown release notes with visible APK download progress',
            'Optional SHA-256 verification before installer handoff',
            'JUnit coverage, Gradle build verification, and signed release support'
          )
        )
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
