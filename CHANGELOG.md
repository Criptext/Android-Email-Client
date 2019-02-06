# Change Log

## [Unreleased](https://github.com/criptext/Android-Email-Client/tree/HEAD)

[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.16.0...HEAD)

**Implemented enhancements:**

- Push Notification Encrypted Preview [\#408](https://github.com/Criptext/Android-Email-Client/issues/408)
- Add UUID to label related events [\#407](https://github.com/Criptext/Android-Email-Client/issues/407)
- Add more rules to the sign up password [\#183](https://github.com/Criptext/Android-Email-Client/issues/183)
- Use a more appropriate adapter for Feed [\#148](https://github.com/Criptext/Android-Email-Client/issues/148)
- Don't load fonts when binding views in a RecyclerView. [\#147](https://github.com/Criptext/Android-Email-Client/issues/147)
- Improve search query instrumentation tests. [\#83](https://github.com/Criptext/Android-Email-Client/issues/83)

**Closed issues:**

- Leave Forward Content Open [\#414](https://github.com/Criptext/Android-Email-Client/issues/414)
- Multiple Accounts [\#409](https://github.com/Criptext/Android-Email-Client/issues/409)
- add column UUID in tables Label and Contact [\#238](https://github.com/Criptext/Android-Email-Client/issues/238)
- Special behavior with some recipientIds  [\#225](https://github.com/Criptext/Android-Email-Client/issues/225)
- Use a file to save email body instead of saving in DB [\#223](https://github.com/Criptext/Android-Email-Client/issues/223)
- Don't open links in the same email viewer [\#190](https://github.com/Criptext/Android-Email-Client/issues/190)
- Create custom app for support@criptext.com [\#188](https://github.com/Criptext/Android-Email-Client/issues/188)
- Create popup for external emails [\#144](https://github.com/Criptext/Android-Email-Client/issues/144)
- Validate if a file is uploading before send an email [\#140](https://github.com/Criptext/Android-Email-Client/issues/140)
- Recieve event: file donwload and show it on activity feed [\#131](https://github.com/Criptext/Android-Email-Client/issues/131)
-  Sent event: file downloaded  [\#130](https://github.com/Criptext/Android-Email-Client/issues/130)
- Show 3 points icon highlighted when you open/close quoted body [\#87](https://github.com/Criptext/Android-Email-Client/issues/87)

**Merged pull requests:**

- Added missing tests and also fixed name on mailbox and added 3 points states for replies and fw. [\#406](https://github.com/Criptext/Android-Email-Client/pull/406) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed the 3 point menu in email detail on spam label. [\#405](https://github.com/Criptext/Android-Email-Client/pull/405) ([jorgeblacio](https://github.com/jorgeblacio))
- Various Fixes [\#404](https://github.com/Criptext/Android-Email-Client/pull/404) ([jorgeblacio](https://github.com/jorgeblacio))
- Now models that need init values are properly recovered after app suspension. [\#403](https://github.com/Criptext/Android-Email-Client/pull/403) ([jorgeblacio](https://github.com/jorgeblacio))
- Now sending file keys for externals and also avoiding unsend on peer issue. [\#402](https://github.com/Criptext/Android-Email-Client/pull/402) ([jorgeblacio](https://github.com/jorgeblacio))
- Various fixes. [\#401](https://github.com/Criptext/Android-Email-Client/pull/401) ([jorgeblacio](https://github.com/jorgeblacio))
- Now we run android tests using firebase. [\#400](https://github.com/Criptext/Android-Email-Client/pull/400) ([jorgeblacio](https://github.com/jorgeblacio))
- Now when clicking on the drawer profile pic takes you to the profile activity. [\#399](https://github.com/Criptext/Android-Email-Client/pull/399) ([jorgeblacio](https://github.com/jorgeblacio))
- Contact filter and Profile pic resize maintaining aspect ratio. [\#398](https://github.com/Criptext/Android-Email-Client/pull/398) ([jorgeblacio](https://github.com/jorgeblacio))
- Prep for release, also fixed a small error with notifiation sound on pull down. [\#397](https://github.com/Criptext/Android-Email-Client/pull/397) ([jorgeblacio](https://github.com/jorgeblacio))
- Hot fix for release 0.16.4 [\#396](https://github.com/Criptext/Android-Email-Client/pull/396) ([jorgeblacio](https://github.com/jorgeblacio))
- Changes to how you change your name. Also peer event for Profile Name. [\#395](https://github.com/Criptext/Android-Email-Client/pull/395) ([jorgeblacio](https://github.com/jorgeblacio))
- Fix crash on reply with atachments, also removed all non resource text and now using native image picker for profile [\#394](https://github.com/Criptext/Android-Email-Client/pull/394) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed filter menu colors on dark theme and also the filtering now works as intended. [\#393](https://github.com/Criptext/Android-Email-Client/pull/393) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed an issue on sending fw with attachments and also the blinking o… [\#392](https://github.com/Criptext/Android-Email-Client/pull/392) ([jorgeblacio](https://github.com/jorgeblacio))
- Added profile settings section to update avatar picture and user full name. [\#391](https://github.com/Criptext/Android-Email-Client/pull/391) ([jorgeblacio](https://github.com/jorgeblacio))
- Hot fix for release 0.16.2 [\#390](https://github.com/Criptext/Android-Email-Client/pull/390) ([jorgeblacio](https://github.com/jorgeblacio))
- Prep for release hot fixes. [\#389](https://github.com/Criptext/Android-Email-Client/pull/389) ([jorgeblacio](https://github.com/jorgeblacio))
- Now on inline images display, those files won't show as attachments, also thy are saved in the app private dir [\#388](https://github.com/Criptext/Android-Email-Client/pull/388) ([jorgeblacio](https://github.com/jorgeblacio))
- Prep for release version 0.16.2 [\#387](https://github.com/Criptext/Android-Email-Client/pull/387) ([jorgeblacio](https://github.com/jorgeblacio))
- Now we are showing inline images in email detail [\#386](https://github.com/Criptext/Android-Email-Client/pull/386) ([jorgeblacio](https://github.com/jorgeblacio))
- Added a retry option to send emails. Also Added headers to the sync devices. [\#385](https://github.com/Criptext/Android-Email-Client/pull/385) ([jorgeblacio](https://github.com/jorgeblacio))
- Now new prekeys get sent when you run out of them. [\#384](https://github.com/Criptext/Android-Email-Client/pull/384) ([jorgeblacio](https://github.com/jorgeblacio))
- Hot fix for release 0.16.0 [\#383](https://github.com/Criptext/Android-Email-Client/pull/383) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can see external email's source [\#382](https://github.com/Criptext/Android-Email-Client/pull/382) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.16.0](https://github.com/criptext/Android-Email-Client/tree/v0.16.0) (2019-01-21)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.15.1...v0.16.0)

**Merged pull requests:**

- Prep for release. [\#381](https://github.com/Criptext/Android-Email-Client/pull/381) ([jorgeblacio](https://github.com/jorgeblacio))
- Changed Reply To option to a new screen. [\#380](https://github.com/Criptext/Android-Email-Client/pull/380) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can see the email source of incoming emails. [\#379](https://github.com/Criptext/Android-Email-Client/pull/379) ([jorgeblacio](https://github.com/jorgeblacio))
- Resend expiration, contactFrom without name fix, key and iv from file instead of filekey table. [\#378](https://github.com/Criptext/Android-Email-Client/pull/378) ([jorgeblacio](https://github.com/jorgeblacio))
- Now email bodies are saved in the file system instead of the db. [\#377](https://github.com/Criptext/Android-Email-Client/pull/377) ([jorgeblacio](https://github.com/jorgeblacio))
- Collapsed emails in email detail. [\#376](https://github.com/Criptext/Android-Email-Client/pull/376) ([jorgeblacio](https://github.com/jorgeblacio))
- Added a couple of needed fields to the db [\#375](https://github.com/Criptext/Android-Email-Client/pull/375) ([jorgeblacio](https://github.com/jorgeblacio))
- Save filekey in file when linking [\#374](https://github.com/Criptext/Android-Email-Client/pull/374) ([saulmestanza](https://github.com/saulmestanza))
- Upgrade to Android X [\#356](https://github.com/Criptext/Android-Email-Client/pull/356) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.15.1](https://github.com/criptext/Android-Email-Client/tree/v0.15.1) (2019-01-14)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.15.0...v0.15.1)

**Fixed bugs:**

- Multi select in mailbox doesn't work correctly sometimes. [\#263](https://github.com/Criptext/Android-Email-Client/issues/263)

**Closed issues:**

- Javascript injection in Criptext 0.14.2 [\#332](https://github.com/Criptext/Android-Email-Client/issues/332)
- Access to contacts  [\#317](https://github.com/Criptext/Android-Email-Client/issues/317)
- Dark Theme [\#270](https://github.com/Criptext/Android-Email-Client/issues/270)
- Use only the first name in the recipients names list [\#264](https://github.com/Criptext/Android-Email-Client/issues/264)
- Sound and vibration when an email arrives inside app [\#118](https://github.com/Criptext/Android-Email-Client/issues/118)

**Merged pull requests:**

- Updated various minor issues. [\#373](https://github.com/Criptext/Android-Email-Client/pull/373) ([jorgeblacio](https://github.com/jorgeblacio))
- Updated text of empty mailboxes. [\#372](https://github.com/Criptext/Android-Email-Client/pull/372) ([jorgeblacio](https://github.com/jorgeblacio))
- Banner update operator implementation and fixed a the empty mailbox image position a bit. [\#371](https://github.com/Criptext/Android-Email-Client/pull/371) ([jorgeblacio](https://github.com/jorgeblacio))
- New assets for empty mailboxes. [\#370](https://github.com/Criptext/Android-Email-Client/pull/370) ([jorgeblacio](https://github.com/jorgeblacio))
- Now banner for updates check for app version and also event pagination is now in place. [\#369](https://github.com/Criptext/Android-Email-Client/pull/369) ([jorgeblacio](https://github.com/jorgeblacio))
- Refactor of mailbox update request [\#368](https://github.com/Criptext/Android-Email-Client/pull/368) ([jorgeblacio](https://github.com/jorgeblacio))
- Added print option for emails. [\#367](https://github.com/Criptext/Android-Email-Client/pull/367) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.15.0](https://github.com/criptext/Android-Email-Client/tree/v0.15.0) (2019-01-04)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.14.7...v0.15.0)

**Merged pull requests:**

- File keys per archive [\#366](https://github.com/Criptext/Android-Email-Client/pull/366) ([saulmestanza](https://github.com/saulmestanza))
- Manual mailbox sync from settings. [\#365](https://github.com/Criptext/Android-Email-Client/pull/365) ([jorgeblacio](https://github.com/jorgeblacio))
- Notifications [\#362](https://github.com/Criptext/Android-Email-Client/pull/362) ([saulmestanza](https://github.com/saulmestanza))

## [v0.14.7](https://github.com/criptext/Android-Email-Client/tree/v0.14.7) (2019-01-02)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.14.6...v0.14.7)

**Merged pull requests:**

- Release Dark Theme [\#364](https://github.com/Criptext/Android-Email-Client/pull/364) ([jorgeblacio](https://github.com/jorgeblacio))
- Change link colors and attachment dialog text color based on theme. [\#363](https://github.com/Criptext/Android-Email-Client/pull/363) ([jorgeblacio](https://github.com/jorgeblacio))
- Various fixes and optimizarions. [\#360](https://github.com/Criptext/Android-Email-Client/pull/360) ([jorgeblacio](https://github.com/jorgeblacio))
- Corrections - Quick Guide, Max files size, device types, header [\#359](https://github.com/Criptext/Android-Email-Client/pull/359) ([saulmestanza](https://github.com/saulmestanza))
- Dark theme [\#358](https://github.com/Criptext/Android-Email-Client/pull/358) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.14.6](https://github.com/criptext/Android-Email-Client/tree/v0.14.6) (2018-12-19)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.14.5...v0.14.6)

**Implemented enhancements:**

- validate file exists to not download it again [\#151](https://github.com/Criptext/Android-Email-Client/issues/151)

**Closed issues:**

- Keep attachments at forwarding an email [\#218](https://github.com/Criptext/Android-Email-Client/issues/218)
- Quick Guide [\#180](https://github.com/Criptext/Android-Email-Client/issues/180)
- Save files in drafts [\#120](https://github.com/Criptext/Android-Email-Client/issues/120)

**Merged pull requests:**

- Final fixes spanish language. [\#357](https://github.com/Criptext/Android-Email-Client/pull/357) ([jorgeblacio](https://github.com/jorgeblacio))
- Minor fixes for spanish release. Signup error handling. [\#355](https://github.com/Criptext/Android-Email-Client/pull/355) ([jorgeblacio](https://github.com/jorgeblacio))
- Minor changes for release. [\#354](https://github.com/Criptext/Android-Email-Client/pull/354) ([jorgeblacio](https://github.com/jorgeblacio))
- Lokalise: Translations update [\#353](https://github.com/Criptext/Android-Email-Client/pull/353) ([danieltigse](https://github.com/danieltigse))
- Now using endpoint for refreshig legacy users token, and some language fixes. [\#351](https://github.com/Criptext/Android-Email-Client/pull/351) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed some minor issues. [\#350](https://github.com/Criptext/Android-Email-Client/pull/350) ([jorgeblacio](https://github.com/jorgeblacio))
- Added refresh session token functionality. [\#349](https://github.com/Criptext/Android-Email-Client/pull/349) ([jorgeblacio](https://github.com/jorgeblacio))
- Added support for spanish language. [\#297](https://github.com/Criptext/Android-Email-Client/pull/297) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.14.5](https://github.com/criptext/Android-Email-Client/tree/v0.14.5) (2018-12-10)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.14.3...v0.14.5)

**Merged pull requests:**

- Version number change for release and minor fix for error push. [\#348](https://github.com/Criptext/Android-Email-Client/pull/348) ([jorgeblacio](https://github.com/jorgeblacio))
- Some fixes to some cases of pin usage. [\#347](https://github.com/Criptext/Android-Email-Client/pull/347) ([jorgeblacio](https://github.com/jorgeblacio))
- Added a new option for privacy and moved email preview and read receipts there. [\#346](https://github.com/Criptext/Android-Email-Client/pull/346) ([jorgeblacio](https://github.com/jorgeblacio))
- AndroidTest Corrections, and implementation of attach files quantity test [\#345](https://github.com/Criptext/Android-Email-Client/pull/345) ([saulmestanza](https://github.com/saulmestanza))
- Added a fix for samsung sqlite queries. [\#344](https://github.com/Criptext/Android-Email-Client/pull/344) ([jorgeblacio](https://github.com/jorgeblacio))
- Various minor adjustments for the code and also managed externals and devices order sorting. [\#343](https://github.com/Criptext/Android-Email-Client/pull/343) ([jorgeblacio](https://github.com/jorgeblacio))
- Attachments in drafts correction [\#342](https://github.com/Criptext/Android-Email-Client/pull/342) ([saulmestanza](https://github.com/saulmestanza))
- Extension file error [\#341](https://github.com/Criptext/Android-Email-Client/pull/341) ([saulmestanza](https://github.com/saulmestanza))
- Upload file error when exceeds max size  [\#340](https://github.com/Criptext/Android-Email-Client/pull/340) ([saulmestanza](https://github.com/saulmestanza))
- Attchaments bug fix and progress bar bug fix. [\#339](https://github.com/Criptext/Android-Email-Client/pull/339) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you are able to delete your account from settings. [\#338](https://github.com/Criptext/Android-Email-Client/pull/338) ([jorgeblacio](https://github.com/jorgeblacio))
- StartGuide attachments design correction, Progress Bar new design in SignIn and Linking Scene [\#337](https://github.com/Criptext/Android-Email-Client/pull/337) ([saulmestanza](https://github.com/saulmestanza))
- Fixed an issue with fw of attchments that had no fileKey [\#336](https://github.com/Criptext/Android-Email-Client/pull/336) ([jorgeblacio](https://github.com/jorgeblacio))
- Added update banner functionality. [\#328](https://github.com/Criptext/Android-Email-Client/pull/328) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.14.3](https://github.com/criptext/Android-Email-Client/tree/v0.14.3) (2018-12-03)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.14.2...v0.14.3)

**Merged pull requests:**

- Added pagination for the creation of the link devices file. [\#335](https://github.com/Criptext/Android-Email-Client/pull/335) ([jorgeblacio](https://github.com/jorgeblacio))
- Startguide fixes  [\#334](https://github.com/Criptext/Android-Email-Client/pull/334) ([saulmestanza](https://github.com/saulmestanza))
- Changed all logout texts for sign out. [\#333](https://github.com/Criptext/Android-Email-Client/pull/333) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed the push notification for opens. [\#331](https://github.com/Criptext/Android-Email-Client/pull/331) ([jorgeblacio](https://github.com/jorgeblacio))
- Updated and added some alert dialogs. [\#330](https://github.com/Criptext/Android-Email-Client/pull/330) ([jorgeblacio](https://github.com/jorgeblacio))
- Startguide [\#329](https://github.com/Criptext/Android-Email-Client/pull/329) ([saulmestanza](https://github.com/saulmestanza))
- Added rate limits messages and a couple more tweaks and features. [\#327](https://github.com/Criptext/Android-Email-Client/pull/327) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.14.2](https://github.com/criptext/Android-Email-Client/tree/v0.14.2) (2018-11-27)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.13.7...v0.14.2)

**Fixed bugs:**

- Scrolling in mailbox during multi select triggers "load more" event and app ends in inconstent state [\#262](https://github.com/Criptext/Android-Email-Client/issues/262)

**Closed issues:**

- Limit recipients to 100 [\#272](https://github.com/Criptext/Android-Email-Client/issues/272)
- get contacts from phonebook at signUp or signIn [\#219](https://github.com/Criptext/Android-Email-Client/issues/219)
- Show uploading percentage immediately after pick a file [\#204](https://github.com/Criptext/Android-Email-Client/issues/204)
- Change file picker [\#119](https://github.com/Criptext/Android-Email-Client/issues/119)

**Merged pull requests:**

- Added some necessary null-checks for the pin lock manager. [\#326](https://github.com/Criptext/Android-Email-Client/pull/326) ([jorgeblacio](https://github.com/jorgeblacio))
- Various minor post release fixes. [\#325](https://github.com/Criptext/Android-Email-Client/pull/325) ([jorgeblacio](https://github.com/jorgeblacio))
- Pin lock is now based on the aesthetic design. [\#324](https://github.com/Criptext/Android-Email-Client/pull/324) ([jorgeblacio](https://github.com/jorgeblacio))
- Forward with attachments. [\#323](https://github.com/Criptext/Android-Email-Client/pull/323) ([jorgeblacio](https://github.com/jorgeblacio))
- Finished all Pin Lock functionality. [\#322](https://github.com/Criptext/Android-Email-Client/pull/322) ([jorgeblacio](https://github.com/jorgeblacio))
- You can now share pictures through Criptext, also you can use mailto. [\#321](https://github.com/Criptext/Android-Email-Client/pull/321) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can enable/disable read receipts in settings. [\#320](https://github.com/Criptext/Android-Email-Client/pull/320) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can lock the app with a pin. [\#319](https://github.com/Criptext/Android-Email-Client/pull/319) ([jorgeblacio](https://github.com/jorgeblacio))
- Users can now sync phonebook contacts from settings. [\#318](https://github.com/Criptext/Android-Email-Client/pull/318) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.13.7](https://github.com/criptext/Android-Email-Client/tree/v0.13.7) (2018-11-19)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.13.6...v0.13.7)

**Merged pull requests:**

- Changed version for release. [\#316](https://github.com/Criptext/Android-Email-Client/pull/316) ([jorgeblacio](https://github.com/jorgeblacio))
- Using native file picker for files. [\#315](https://github.com/Criptext/Android-Email-Client/pull/315) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a crash that happened only on Kitkat when starting the app. [\#314](https://github.com/Criptext/Android-Email-Client/pull/314) ([jorgeblacio](https://github.com/jorgeblacio))
- Now the toolbar shows the correct number of unread emails. [\#313](https://github.com/Criptext/Android-Email-Client/pull/313) ([jorgeblacio](https://github.com/jorgeblacio))
- New emails won't reload the whole recycler view and show a snack bar with quantity. [\#312](https://github.com/Criptext/Android-Email-Client/pull/312) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.13.6](https://github.com/criptext/Android-Email-Client/tree/v0.13.6) (2018-11-12)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.13.5...v0.13.6)

**Merged pull requests:**

- Fix a double ringing on notifications. [\#311](https://github.com/Criptext/Android-Email-Client/pull/311) ([jorgeblacio](https://github.com/jorgeblacio))
- Made various improvements to the update of the recycler view of the email threads. [\#310](https://github.com/Criptext/Android-Email-Client/pull/310) ([jorgeblacio](https://github.com/jorgeblacio))
- Now the push notifications don't use the data from the push payload. [\#309](https://github.com/Criptext/Android-Email-Client/pull/309) ([jorgeblacio](https://github.com/jorgeblacio))
- Notifications now act faster on action buttons and Mailbox Update issue. [\#308](https://github.com/Criptext/Android-Email-Client/pull/308) ([jorgeblacio](https://github.com/jorgeblacio))
- Refactored the way postDelays were handled. [\#307](https://github.com/Criptext/Android-Email-Client/pull/307) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.13.5](https://github.com/criptext/Android-Email-Client/tree/v0.13.5) (2018-11-05)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.13.4...v0.13.5)

**Merged pull requests:**

- Refactor notification code. [\#306](https://github.com/Criptext/Android-Email-Client/pull/306) ([jorgeblacio](https://github.com/jorgeblacio))
- empty email detail fix and criptext quote standard implemented [\#305](https://github.com/Criptext/Android-Email-Client/pull/305) ([danieltigse](https://github.com/danieltigse))
- Fixed various issues. [\#304](https://github.com/Criptext/Android-Email-Client/pull/304) ([jorgeblacio](https://github.com/jorgeblacio))
- Added support for .zip and audio files on the picker. [\#303](https://github.com/Criptext/Android-Email-Client/pull/303) ([jorgeblacio](https://github.com/jorgeblacio))
- Added reply button and refactored notification code a bit [\#302](https://github.com/Criptext/Android-Email-Client/pull/302) ([jorgeblacio](https://github.com/jorgeblacio))
- Added queue to handle peer event dispatching. [\#300](https://github.com/Criptext/Android-Email-Client/pull/300) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.13.4](https://github.com/criptext/Android-Email-Client/tree/v0.13.4) (2018-10-29)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/0.13.2...v0.13.4)

**Closed issues:**

- Have a single global data source to avoid repeating code in the DB and API requirements on the different controllers. [\#232](https://github.com/Criptext/Android-Email-Client/issues/232)
- Load email even if the images haven't loaded yet [\#202](https://github.com/Criptext/Android-Email-Client/issues/202)
- Open app when you tap on a grouped notification [\#201](https://github.com/Criptext/Android-Email-Client/issues/201)

**Merged pull requests:**

- Release 0.13.4 [\#301](https://github.com/Criptext/Android-Email-Client/pull/301) ([jorgeblacio](https://github.com/jorgeblacio))
- Reply on push notification and bug fixes. [\#299](https://github.com/Criptext/Android-Email-Client/pull/299) ([jorgeblacio](https://github.com/jorgeblacio))
- Made it so that the notification only appear after the update has been processed. [\#298](https://github.com/Criptext/Android-Email-Client/pull/298) ([jorgeblacio](https://github.com/jorgeblacio))
- Added device types for connection holder and some other tweaks. [\#296](https://github.com/Criptext/Android-Email-Client/pull/296) ([jorgeblacio](https://github.com/jorgeblacio))

## [0.13.2](https://github.com/criptext/Android-Email-Client/tree/0.13.2) (2018-10-19)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.13.0...0.13.2)

**Closed issues:**

- Add header API-Version=1.0 to all the requirements  [\#196](https://github.com/Criptext/Android-Email-Client/issues/196)
- Link devices [\#129](https://github.com/Criptext/Android-Email-Client/issues/129)

**Merged pull requests:**

- Added the ui changes for 2FA and some error handling. [\#295](https://github.com/Criptext/Android-Email-Client/pull/295) ([jorgeblacio](https://github.com/jorgeblacio))
- zoom, pan and drag for email detail webview [\#294](https://github.com/Criptext/Android-Email-Client/pull/294) ([danieltigse](https://github.com/danieltigse))
- Made some tweaks to the mailbox ui. [\#293](https://github.com/Criptext/Android-Email-Client/pull/293) ([jorgeblacio](https://github.com/jorgeblacio))
- Now the user can set and use 2-factor authentication. [\#292](https://github.com/Criptext/Android-Email-Client/pull/292) ([jorgeblacio](https://github.com/jorgeblacio))
- Added options for mark as read and move to trash on push notification. [\#291](https://github.com/Criptext/Android-Email-Client/pull/291) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a crash when old device times out [\#290](https://github.com/Criptext/Android-Email-Client/pull/290) ([jorgeblacio](https://github.com/jorgeblacio))
- Various minor Fixes [\#289](https://github.com/Criptext/Android-Email-Client/pull/289) ([jorgeblacio](https://github.com/jorgeblacio))
- Removed jigl hosts for open source release. [\#288](https://github.com/Criptext/Android-Email-Client/pull/288) ([jorgeblacio](https://github.com/jorgeblacio))
- Changelog for release 0.13.0 [\#287](https://github.com/Criptext/Android-Email-Client/pull/287) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.13.0](https://github.com/criptext/Android-Email-Client/tree/v0.13.0) (2018-10-15)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.12.1...v0.13.0)

**Merged pull requests:**

- Added retry counters and also full approve from notification. [\#286](https://github.com/Criptext/Android-Email-Client/pull/286) ([jorgeblacio](https://github.com/jorgeblacio))
- Refactored link devices flow to not be solely dependant on web socket events. [\#285](https://github.com/Criptext/Android-Email-Client/pull/285) ([jorgeblacio](https://github.com/jorgeblacio))
- Devices now link and share data to login. [\#281](https://github.com/Criptext/Android-Email-Client/pull/281) ([jorgeblacio](https://github.com/jorgeblacio))

## [v0.12.1](https://github.com/criptext/Android-Email-Client/tree/v0.12.1) (2018-10-03)
[Full Changelog](https://github.com/criptext/Android-Email-Client/compare/v0.12.0...v0.12.1)

**Closed issues:**

- Don't back to mailbox when you starred an email [\#199](https://github.com/Criptext/Android-Email-Client/issues/199)

**Merged pull requests:**

- Added limits and some user growth features. [\#284](https://github.com/Criptext/Android-Email-Client/pull/284) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can approve or deny device auth on push notifications. [\#283](https://github.com/Criptext/Android-Email-Client/pull/283) ([jorgeblacio](https://github.com/jorgeblacio))
- Now we use the staging server, and added api version header. [\#282](https://github.com/Criptext/Android-Email-Client/pull/282) ([jorgeblacio](https://github.com/jorgeblacio))
- Updated changelog after release. [\#280](https://github.com/Criptext/Android-Email-Client/pull/280) ([jorgeblacio](https://github.com/jorgeblacio))
- Update issue templates [\#275](https://github.com/Criptext/Android-Email-Client/pull/275) ([danieltigse](https://github.com/danieltigse))

## [v0.12.0](https://github.com/criptext/Android-Email-Client/tree/v0.12.0) (2018-10-01)
**Fixed bugs:**

- Show only deleted emails in Trash folder [\#182](https://github.com/Criptext/Android-Email-Client/issues/182)
- Rich editor doesn't work for forwarded emails [\#138](https://github.com/Criptext/Android-Email-Client/issues/138)
- Don't show inbox and sent labels in emailDetailView [\#114](https://github.com/Criptext/Android-Email-Client/issues/114)
- Do not refresh all mailbox after an action \(marks as unread, delete\) [\#95](https://github.com/Criptext/Android-Email-Client/issues/95)
- Crash at deleting a thread [\#93](https://github.com/Criptext/Android-Email-Client/issues/93)
- When you move an email that has label Inbox to Inbox it crash [\#92](https://github.com/Criptext/Android-Email-Client/issues/92)

**Closed issues:**

- Ignore params in websocket, just call GET events [\#254](https://github.com/Criptext/Android-Email-Client/issues/254)
- add Reset Password in Settings [\#242](https://github.com/Criptext/Android-Email-Client/issues/242)
- Don't send ack for 302 events if the email doesn't exists [\#240](https://github.com/Criptext/Android-Email-Client/issues/240)
- Set label Spam when the event params contains `labels` object [\#233](https://github.com/Criptext/Android-Email-Client/issues/233)
- Show banner for "Empty trash" in trash folder [\#229](https://github.com/Criptext/Android-Email-Client/issues/229)
- Implement Reset password [\#215](https://github.com/Criptext/Android-Email-Client/issues/215)
- Implement change password [\#214](https://github.com/Criptext/Android-Email-Client/issues/214)
- Disable unsend button if the emails is sent unencrypted \(including mixed recipients\) [\#208](https://github.com/Criptext/Android-Email-Client/issues/208)
- Recover from trash doesn't work [\#206](https://github.com/Criptext/Android-Email-Client/issues/206)
- Implement change recovery email [\#198](https://github.com/Criptext/Android-Email-Client/issues/198)
- Add more information in the email for support [\#195](https://github.com/Criptext/Android-Email-Client/issues/195)
- Upgrade DB version to fix contacts [\#194](https://github.com/Criptext/Android-Email-Client/issues/194)
- Logout device if you receive http code 401 in any requirement  [\#193](https://github.com/Criptext/Android-Email-Client/issues/193)
- Receive command of password changed [\#192](https://github.com/Criptext/Android-Email-Client/issues/192)
- Don't delete database until you log in with a different account [\#189](https://github.com/Criptext/Android-Email-Client/issues/189)
- Don't allow special characters in the username in SignUp forms [\#187](https://github.com/Criptext/Android-Email-Client/issues/187)
- Change characters minimum to 3 in the validation for external email password  [\#186](https://github.com/Criptext/Android-Email-Client/issues/186)
- Welcome Tour [\#181](https://github.com/Criptext/Android-Email-Client/issues/181)
- Composer: Send password encrypted emails to non-criptext users. [\#163](https://github.com/Criptext/Android-Email-Client/issues/163)
- Logout [\#155](https://github.com/Criptext/Android-Email-Client/issues/155)
- Email Detail: Unsend [\#154](https://github.com/Criptext/Android-Email-Client/issues/154)
- Settings: Remove device [\#153](https://github.com/Criptext/Android-Email-Client/issues/153)
- Composer: Send email to non-criptext users [\#152](https://github.com/Criptext/Android-Email-Client/issues/152)
- Implement endpoint to edit fullname [\#141](https://github.com/Criptext/Android-Email-Client/issues/141)
- Handle sending status [\#139](https://github.com/Criptext/Android-Email-Client/issues/139)
- Peer Events Handling [\#132](https://github.com/Criptext/Android-Email-Client/issues/132)
- News and Older sections in Activity Feed List [\#127](https://github.com/Criptext/Android-Email-Client/issues/127)
- Activity feed buttons \(mute and delete\) bigger [\#126](https://github.com/Criptext/Android-Email-Client/issues/126)
- Sign in Button should be inactive on start  [\#125](https://github.com/Criptext/Android-Email-Client/issues/125)
- Restore File Table  [\#124](https://github.com/Criptext/Android-Email-Client/issues/124)
- Create custom labels [\#121](https://github.com/Criptext/Android-Email-Client/issues/121)
- FileView don't have borders in EmailDetailView [\#116](https://github.com/Criptext/Android-Email-Client/issues/116)
- Change labels of NoMails View  [\#115](https://github.com/Criptext/Android-Email-Client/issues/115)
- Center vertically all custom dialogs [\#111](https://github.com/Criptext/Android-Email-Client/issues/111)
- Implement labels and devices settings  [\#103](https://github.com/Criptext/Android-Email-Client/issues/103)
- Implement Activity Feed [\#102](https://github.com/Criptext/Android-Email-Client/issues/102)
- Add privacy policy in settings [\#101](https://github.com/Criptext/Android-Email-Client/issues/101)
- Change all mail icon in menu [\#100](https://github.com/Criptext/Android-Email-Client/issues/100)
- Change support icon in menu [\#99](https://github.com/Criptext/Android-Email-Client/issues/99)
- Remove circle from x button in search scene [\#98](https://github.com/Criptext/Android-Email-Client/issues/98)
- Change "No Search History" & "No search results" design [\#97](https://github.com/Criptext/Android-Email-Client/issues/97)
- Save a search history when you click one of the result emails [\#96](https://github.com/Criptext/Android-Email-Client/issues/96)
- Preview text more to the right [\#94](https://github.com/Criptext/Android-Email-Client/issues/94)
- Change label Remove from trash to Move to inbox [\#91](https://github.com/Criptext/Android-Email-Client/issues/91)
- Email detail are not showing CC & BCC [\#90](https://github.com/Criptext/Android-Email-Client/issues/90)
- Implement new email detail design [\#89](https://github.com/Criptext/Android-Email-Client/issues/89)
- Add all\(sender and  date\) text inside quoted text [\#88](https://github.com/Criptext/Android-Email-Client/issues/88)
- Open all unread emails in email detail scene [\#86](https://github.com/Criptext/Android-Email-Client/issues/86)
- Remove Important Label [\#85](https://github.com/Criptext/Android-Email-Client/issues/85)
- Include ourself in the getKeyBundle and set us as "from" at sending email [\#84](https://github.com/Criptext/Android-Email-Client/issues/84)
- Create `SearchHistoryManager` class. [\#82](https://github.com/Criptext/Android-Email-Client/issues/82)
- `VirtualListAdapter` should render a special view when the virtual list is empty [\#81](https://github.com/Criptext/Android-Email-Client/issues/81)

**Merged pull requests:**

- Upgrade version for release. [\#279](https://github.com/Criptext/Android-Email-Client/pull/279) ([jorgeblacio](https://github.com/jorgeblacio))
- Refactored the parse events into a single object. [\#278](https://github.com/Criptext/Android-Email-Client/pull/278) ([jorgeblacio](https://github.com/jorgeblacio))
- Activated the first part of link devices for release. [\#277](https://github.com/Criptext/Android-Email-Client/pull/277) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed an issue when user had an untrimed name. [\#276](https://github.com/Criptext/Android-Email-Client/pull/276) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a couple of mailbox issues. [\#274](https://github.com/Criptext/Android-Email-Client/pull/274) ([jorgeblacio](https://github.com/jorgeblacio))
- Update issue templates [\#273](https://github.com/Criptext/Android-Email-Client/pull/273) ([danieltigse](https://github.com/danieltigse))
- Removed bluetooth usage. [\#271](https://github.com/Criptext/Android-Email-Client/pull/271) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed an issue with sending external encrypted emails. [\#269](https://github.com/Criptext/Android-Email-Client/pull/269) ([jorgeblacio](https://github.com/jorgeblacio))
- First part of link devices, the remote authentication is working. [\#268](https://github.com/Criptext/Android-Email-Client/pull/268) ([jorgeblacio](https://github.com/jorgeblacio))
- Changed the websocket to only parse one event to do get\_events. [\#267](https://github.com/Criptext/Android-Email-Client/pull/267) ([jorgeblacio](https://github.com/jorgeblacio))
- Now Recovery Email scene updates trhough the web socket event. [\#266](https://github.com/Criptext/Android-Email-Client/pull/266) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a bug that moved the emails to ALL\_EMAIL when you recover from trash. \[206\] [\#265](https://github.com/Criptext/Android-Email-Client/pull/265) ([jorgeblacio](https://github.com/jorgeblacio))
- Various aesthetic fixes. [\#261](https://github.com/Criptext/Android-Email-Client/pull/261) ([jorgeblacio](https://github.com/jorgeblacio))
- Now web socket only lets it's listeners now that there is a new event for them to do get events. [\#260](https://github.com/Criptext/Android-Email-Client/pull/260) ([jorgeblacio](https://github.com/jorgeblacio))
- resend emails only if there is not activityMessage [\#259](https://github.com/Criptext/Android-Email-Client/pull/259) ([danieltigse](https://github.com/danieltigse))
- Fixed db migration update to contacts in case there is a conflict in email. [\#258](https://github.com/Criptext/Android-Email-Client/pull/258) ([jorgeblacio](https://github.com/jorgeblacio))
- Changed the db migration to happen in just one step instead of two. [\#257](https://github.com/Criptext/Android-Email-Client/pull/257) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed various minor issues. [\#256](https://github.com/Criptext/Android-Email-Client/pull/256) ([jorgeblacio](https://github.com/jorgeblacio))
- Now emails get resent if they fail. [\#255](https://github.com/Criptext/Android-Email-Client/pull/255) ([jorgeblacio](https://github.com/jorgeblacio))
- Unified the recovery email screen. [\#253](https://github.com/Criptext/Android-Email-Client/pull/253) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed the bug that blinked the mailbox when you resume the app after a logout. [\#252](https://github.com/Criptext/Android-Email-Client/pull/252) ([jorgeblacio](https://github.com/jorgeblacio))
- Added dialog to display hidden email in reset password. [\#251](https://github.com/Criptext/Android-Email-Client/pull/251) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed the trash showing all thread issue. [\#250](https://github.com/Criptext/Android-Email-Client/pull/250) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed an issue when extracting the name when there was no name in the contact address. [\#249](https://github.com/Criptext/Android-Email-Client/pull/249) ([jorgeblacio](https://github.com/jorgeblacio))
- Added the empty trash banner, button funcitonallity and the auto delete on expiration. [\#248](https://github.com/Criptext/Android-Email-Client/pull/248) ([jorgeblacio](https://github.com/jorgeblacio))
- If an incoming email is marked as Spam, it now adds the proper label. [\#246](https://github.com/Criptext/Android-Email-Client/pull/246) ([jorgeblacio](https://github.com/jorgeblacio))
- Migrated the database to fix label names and support trash date of emails. [\#245](https://github.com/Criptext/Android-Email-Client/pull/245) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed script to use the circle ci cache. [\#244](https://github.com/Criptext/Android-Email-Client/pull/244) ([jorgeblacio](https://github.com/jorgeblacio))
- Various fixes [\#243](https://github.com/Criptext/Android-Email-Client/pull/243) ([jorgeblacio](https://github.com/jorgeblacio))
- If the email is not in the data base do not acknowledge the read event for it. [\#241](https://github.com/Criptext/Android-Email-Client/pull/241) ([jorgeblacio](https://github.com/jorgeblacio))
- Added a couple small fixes and tweaks. [\#239](https://github.com/Criptext/Android-Email-Client/pull/239) ([jorgeblacio](https://github.com/jorgeblacio))
- Now on logout the user data is untouched unless you try to login with a different account. [\#237](https://github.com/Criptext/Android-Email-Client/pull/237) ([jorgeblacio](https://github.com/jorgeblacio))
- Added various small fixes. [\#236](https://github.com/Criptext/Android-Email-Client/pull/236) ([jorgeblacio](https://github.com/jorgeblacio))
- Added a better way to discover the remove device feature. [\#234](https://github.com/Criptext/Android-Email-Client/pull/234) ([jorgeblacio](https://github.com/jorgeblacio))
- Updated the devices icons. [\#231](https://github.com/Criptext/Android-Email-Client/pull/231) ([jorgeblacio](https://github.com/jorgeblacio))
- Added the forgot password option and functionality [\#230](https://github.com/Criptext/Android-Email-Client/pull/230) ([jorgeblacio](https://github.com/jorgeblacio))
- Adding CircleCI 2.0 config file [\#228](https://github.com/Criptext/Android-Email-Client/pull/228) ([jorgeblacio](https://github.com/jorgeblacio))
- Flag for the worker to remove device until logout endpoint is ready. [\#226](https://github.com/Criptext/Android-Email-Client/pull/226) ([jorgeblacio](https://github.com/jorgeblacio))
- Password can be changed and the clients can handle it. [\#224](https://github.com/Criptext/Android-Email-Client/pull/224) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a sync bug between the different clients when an email was opened. [\#222](https://github.com/Criptext/Android-Email-Client/pull/222) ([jorgeblacio](https://github.com/jorgeblacio))
- Fix some issues with push notifications in some android devices. [\#221](https://github.com/Criptext/Android-Email-Client/pull/221) ([jorgeblacio](https://github.com/jorgeblacio))
- Added change password and change recovery email options to settings. [\#220](https://github.com/Criptext/Android-Email-Client/pull/220) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a bug that some events were not been acknowledged. [\#217](https://github.com/Criptext/Android-Email-Client/pull/217) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a bug that didn't allow you to move emails around in the mailbox. [\#216](https://github.com/Criptext/Android-Email-Client/pull/216) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed a crashing bug and a validation error for some incoming emails. [\#213](https://github.com/Criptext/Android-Email-Client/pull/213) ([jorgeblacio](https://github.com/jorgeblacio))
- Added the welcome tour. [\#212](https://github.com/Criptext/Android-Email-Client/pull/212) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed all android tests. [\#211](https://github.com/Criptext/Android-Email-Client/pull/211) ([jorgeblacio](https://github.com/jorgeblacio))
- Various minor fixes, and fixed the unread status not saving in the db bug. [\#210](https://github.com/Criptext/Android-Email-Client/pull/210) ([jorgeblacio](https://github.com/jorgeblacio))
- minor fix for event number and code version. [\#207](https://github.com/Criptext/Android-Email-Client/pull/207) ([jorgeblacio](https://github.com/jorgeblacio))
- Added various minor fixes. [\#205](https://github.com/Criptext/Android-Email-Client/pull/205) ([jorgeblacio](https://github.com/jorgeblacio))
- Fixed an issue that was not triggering the Peer Email Read Status cha… [\#179](https://github.com/Criptext/Android-Email-Client/pull/179) ([jorgeblacio](https://github.com/jorgeblacio))
- You can now remove other devices linked to your account. [\#178](https://github.com/Criptext/Android-Email-Client/pull/178) ([jorgeblacio](https://github.com/jorgeblacio))
- Miscellaneous fixes. [\#176](https://github.com/Criptext/Android-Email-Client/pull/176) ([jorgeblacio](https://github.com/jorgeblacio))
- hash sha256 updated to follow the iOS way [\#175](https://github.com/Criptext/Android-Email-Client/pull/175) ([danieltigse](https://github.com/danieltigse))
- Various tweaks for settings and push notifications [\#174](https://github.com/Criptext/Android-Email-Client/pull/174) ([jorgeblacio](https://github.com/jorgeblacio))
- File picker now lets you pick images and take photos. [\#173](https://github.com/Criptext/Android-Email-Client/pull/173) ([jorgeblacio](https://github.com/jorgeblacio))
- welcomeback wasabeef rich editor [\#172](https://github.com/Criptext/Android-Email-Client/pull/172) ([danieltigse](https://github.com/danieltigse))
- Various needed fixes and additions. [\#171](https://github.com/Criptext/Android-Email-Client/pull/171) ([jorgeblacio](https://github.com/jorgeblacio))
- Added various fixes and missing options. [\#170](https://github.com/Criptext/Android-Email-Client/pull/170) ([jorgeblacio](https://github.com/jorgeblacio))
- Now the user can logout, and all data is deleted. [\#169](https://github.com/Criptext/Android-Email-Client/pull/169) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can received push notifications on a new email. [\#168](https://github.com/Criptext/Android-Email-Client/pull/168) ([jorgeblacio](https://github.com/jorgeblacio))
- ux fixes in emailDetail [\#167](https://github.com/Criptext/Android-Email-Client/pull/167) ([danieltigse](https://github.com/danieltigse))
- Changed package name to com.criptext.mail so that we can use firebase push notifications. [\#166](https://github.com/Criptext/Android-Email-Client/pull/166) ([jorgeblacio](https://github.com/jorgeblacio))
- Unsend and Peer events [\#165](https://github.com/Criptext/Android-Email-Client/pull/165) ([jorgeblacio](https://github.com/jorgeblacio))
- You can now send emails to non-criptext users encrypted by a user given password [\#164](https://github.com/Criptext/Android-Email-Client/pull/164) ([jorgeblacio](https://github.com/jorgeblacio))
- changes after weekly review [\#162](https://github.com/Criptext/Android-Email-Client/pull/162) ([danieltigse](https://github.com/danieltigse))
- Encrypt attachments with AES [\#160](https://github.com/Criptext/Android-Email-Client/pull/160) ([jorgeblacio](https://github.com/jorgeblacio))
- Now you can send email to non criptext users. \(\#152\) [\#159](https://github.com/Criptext/Android-Email-Client/pull/159) ([jorgeblacio](https://github.com/jorgeblacio))
- implement changes of signup and signin [\#158](https://github.com/Criptext/Android-Email-Client/pull/158) ([danieltigse](https://github.com/danieltigse))
- Made it so that the data saved by the UserDataWriter is now encryptd by chunks. [\#157](https://github.com/Criptext/Android-Email-Client/pull/157) ([jorgeblacio](https://github.com/jorgeblacio))
- fixes after review [\#156](https://github.com/Criptext/Android-Email-Client/pull/156) ([danieltigse](https://github.com/danieltigse))
- Created worker for saving data for Linking Devices. [\#150](https://github.com/Criptext/Android-Email-Client/pull/150) ([jorgeblacio](https://github.com/jorgeblacio))
- Sending status and some UX fixes [\#149](https://github.com/Criptext/Android-Email-Client/pull/149) ([danieltigse](https://github.com/danieltigse))
- Changed the Background Work Manager to allow multiple attachments uploads. [\#146](https://github.com/Criptext/Android-Email-Client/pull/146) ([jorgeblacio](https://github.com/jorgeblacio))
- Created popup when trying to send an email to non-criptext users. \#144 [\#145](https://github.com/Criptext/Android-Email-Client/pull/145) ([jorgeblacio](https://github.com/jorgeblacio))
- Added the CC and BCC to the Email detail pop up. [\#143](https://github.com/Criptext/Android-Email-Client/pull/143) ([jorgeblacio](https://github.com/jorgeblacio))
- Finished the SearchHistoryManager class, now it use a lazy string list and access storage only when needed. [\#142](https://github.com/Criptext/Android-Email-Client/pull/142) ([jorgeblacio](https://github.com/jorgeblacio))
- create custom labels [\#137](https://github.com/Criptext/Android-Email-Client/pull/137) ([danieltigse](https://github.com/danieltigse))
- activity feed implemented [\#136](https://github.com/Criptext/Android-Email-Client/pull/136) ([danieltigse](https://github.com/danieltigse))
- Created a manager for searchHistory functionality to avoid controller complexity. [\#135](https://github.com/Criptext/Android-Email-Client/pull/135) ([jorgeblacio](https://github.com/jorgeblacio))
- Test errors fixed [\#134](https://github.com/Criptext/Android-Email-Client/pull/134) ([Hirobreak](https://github.com/Hirobreak))
- Fix android tests [\#133](https://github.com/Criptext/Android-Email-Client/pull/133) ([not-gabriel](https://github.com/not-gabriel))
- Changed backticks symbols, since the tests were crashing on windows. [\#128](https://github.com/Criptext/Android-Email-Client/pull/128) ([jorgeblacio](https://github.com/jorgeblacio))
- sending own email to peer devices resolves \#84, receiving own email in inbox [\#123](https://github.com/Criptext/Android-Email-Client/pull/123) ([Hirobreak](https://github.com/Hirobreak))
- new design implemented [\#122](https://github.com/Criptext/Android-Email-Client/pull/122) ([danieltigse](https://github.com/danieltigse))
- Create EmailPreview class [\#113](https://github.com/Criptext/Android-Email-Client/pull/113) ([not-gabriel](https://github.com/not-gabriel))
- settings activity implemented [\#112](https://github.com/Criptext/Android-Email-Client/pull/112) ([danieltigse](https://github.com/danieltigse))
- Show blue marks on read emails [\#109](https://github.com/Criptext/Android-Email-Client/pull/109) ([not-gabriel](https://github.com/not-gabriel))
- send open event in email detail scene [\#108](https://github.com/Criptext/Android-Email-Client/pull/108) ([not-gabriel](https://github.com/not-gabriel))
- remove toggleNoMailsView in MailboxScene [\#107](https://github.com/Criptext/Android-Email-Client/pull/107) ([not-gabriel](https://github.com/not-gabriel))
- Support empty view in VirtualListAdapter [\#106](https://github.com/Criptext/Android-Email-Client/pull/106) ([not-gabriel](https://github.com/not-gabriel))
- Fix android tests [\#105](https://github.com/Criptext/Android-Email-Client/pull/105) ([not-gabriel](https://github.com/not-gabriel))
- Downloads [\#104](https://github.com/Criptext/Android-Email-Client/pull/104) ([Hirobreak](https://github.com/Hirobreak))
- Upload And Send Attachments [\#80](https://github.com/Criptext/Android-Email-Client/pull/80) ([Hirobreak](https://github.com/Hirobreak))
- ux changes in emailDetail and mailbox [\#79](https://github.com/Criptext/Android-Email-Client/pull/79) ([danieltigse](https://github.com/danieltigse))
- search implementation [\#78](https://github.com/Criptext/Android-Email-Client/pull/78) ([danieltigse](https://github.com/danieltigse))
- UX inprovments in signIn, mailbox, slide menu and email detail [\#77](https://github.com/Criptext/Android-Email-Client/pull/77) ([danieltigse](https://github.com/danieltigse))
- Post progress from BackgroundWorkers [\#76](https://github.com/Criptext/Android-Email-Client/pull/76) ([not-gabriel](https://github.com/not-gabriel))
- Test that attachments are uploaded correctly [\#75](https://github.com/Criptext/Android-Email-Client/pull/75) ([not-gabriel](https://github.com/not-gabriel))
- Create UploadAttachmentWorker [\#74](https://github.com/Criptext/Android-Email-Client/pull/74) ([not-gabriel](https://github.com/not-gabriel))
-  Use EmailInsertionSetup in SaveEmailWorker [\#73](https://github.com/Criptext/Android-Email-Client/pull/73) ([not-gabriel](https://github.com/not-gabriel))
- UX fixes in login, mailbox and composer [\#72](https://github.com/Criptext/Android-Email-Client/pull/72) ([danieltigse](https://github.com/danieltigse))
- Add attachment button in composer [\#71](https://github.com/Criptext/Android-Email-Client/pull/71) ([not-gabriel](https://github.com/not-gabriel))
- UX improvments in mailbox and drafts [\#70](https://github.com/Criptext/Android-Email-Client/pull/70) ([danieltigse](https://github.com/danieltigse))
-  Don't delete user data in instrumentation tests [\#69](https://github.com/Criptext/Android-Email-Client/pull/69) ([not-gabriel](https://github.com/not-gabriel))
- Store device id in Account and ActiveAccount [\#68](https://github.com/Criptext/Android-Email-Client/pull/68) ([not-gabriel](https://github.com/not-gabriel))
-  Update send mail worker [\#67](https://github.com/Criptext/Android-Email-Client/pull/67) ([not-gabriel](https://github.com/not-gabriel))
- Sign In with password [\#66](https://github.com/Criptext/Android-Email-Client/pull/66) ([not-gabriel](https://github.com/not-gabriel))
- Create Hosts file for declaring hostnames [\#65](https://github.com/Criptext/Android-Email-Client/pull/65) ([not-gabriel](https://github.com/not-gabriel))
- Delete MailboxSceneControllerTest [\#64](https://github.com/Criptext/Android-Email-Client/pull/64) ([not-gabriel](https://github.com/not-gabriel))
- Delete processed events in UpdateMailboxWorker [\#63](https://github.com/Criptext/Android-Email-Client/pull/63) ([not-gabriel](https://github.com/not-gabriel))
- fetch pending events on Mailbox's onStart [\#62](https://github.com/Criptext/Android-Email-Client/pull/62) ([not-gabriel](https://github.com/not-gabriel))
- Modify LoadEmailThreadsWorker to load from start [\#61](https://github.com/Criptext/Android-Email-Client/pull/61) ([not-gabriel](https://github.com/not-gabriel))
- refactor web socket [\#60](https://github.com/Criptext/Android-Email-Client/pull/60) ([not-gabriel](https://github.com/not-gabriel))
- email detail redisign and workers [\#59](https://github.com/Criptext/Android-Email-Client/pull/59) ([danieltigse](https://github.com/danieltigse))
- New class EmailInsertionSetup [\#58](https://github.com/Criptext/Android-Email-Client/pull/58) ([not-gabriel](https://github.com/not-gabriel))
- Fix mailbox bugs and \(possibly\) add new bugs [\#57](https://github.com/Criptext/Android-Email-Client/pull/57) ([not-gabriel](https://github.com/not-gabriel))
- Change all database ids to Long [\#56](https://github.com/Criptext/Android-Email-Client/pull/56) ([not-gabriel](https://github.com/not-gabriel))
- ui changes: menu and mailbox [\#55](https://github.com/Criptext/Android-Email-Client/pull/55) ([danieltigse](https://github.com/danieltigse))
- New class Label.DefaultItems [\#54](https://github.com/Criptext/Android-Email-Client/pull/54) ([not-gabriel](https://github.com/not-gabriel))
- Close SignInActivity when going to SignUpActivity [\#53](https://github.com/Criptext/Android-Email-Client/pull/53) ([not-gabriel](https://github.com/not-gabriel))
- composer - new changes [\#52](https://github.com/Criptext/Android-Email-Client/pull/52) ([danieltigse](https://github.com/danieltigse))
- Websocket copy [\#51](https://github.com/Criptext/Android-Email-Client/pull/51) ([danieltigse](https://github.com/danieltigse))
- composer - new design [\#49](https://github.com/Criptext/Android-Email-Client/pull/49) ([danieltigse](https://github.com/danieltigse))
- Send email from mailbox instead of composer. [\#47](https://github.com/Criptext/Android-Email-Client/pull/47) ([not-gabriel](https://github.com/not-gabriel))
- Email Detail and Mailbox Fixes I [\#46](https://github.com/Criptext/Android-Email-Client/pull/46) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Send messages between activities [\#45](https://github.com/Criptext/Android-Email-Client/pull/45) ([not-gabriel](https://github.com/not-gabriel))
- CHANGES IN EMAIL DETAIL AND MAILBOX [\#44](https://github.com/Criptext/Android-Email-Client/pull/44) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- fix composer menu bug [\#43](https://github.com/Criptext/Android-Email-Client/pull/43) ([not-gabriel](https://github.com/not-gabriel))
- Send emails in composer [\#42](https://github.com/Criptext/Android-Email-Client/pull/42) ([not-gabriel](https://github.com/not-gabriel))
- Save new active account data in local storage [\#41](https://github.com/Criptext/Android-Email-Client/pull/41) ([not-gabriel](https://github.com/not-gabriel))
- Mailbox - Fixes  [\#40](https://github.com/Criptext/Android-Email-Client/pull/40) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Write signal tests with room [\#39](https://github.com/Criptext/Android-Email-Client/pull/39) ([not-gabriel](https://github.com/not-gabriel))
- Write android test exchanging messages [\#38](https://github.com/Criptext/Android-Email-Client/pull/38) ([not-gabriel](https://github.com/not-gabriel))
- Email detail popupwindows [\#37](https://github.com/Criptext/Android-Email-Client/pull/37) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- refactor DAOs,  added new read history popupwindow, wrapped email details in cardview [\#36](https://github.com/Criptext/Android-Email-Client/pull/36) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- New class RegistrationBundles [\#35](https://github.com/Criptext/Android-Email-Client/pull/35) ([not-gabriel](https://github.com/not-gabriel))
- Email detail Basic layouts and interaction [\#34](https://github.com/Criptext/Android-Email-Client/pull/34) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
-  Save token during user registration  [\#32](https://github.com/Criptext/Android-Email-Client/pull/32) ([not-gabriel](https://github.com/not-gabriel))
- Test [\#31](https://github.com/Criptext/Android-Email-Client/pull/31) ([not-gabriel](https://github.com/not-gabriel))
- Signin [\#30](https://github.com/Criptext/Android-Email-Client/pull/30) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Mailbox [\#29](https://github.com/Criptext/Android-Email-Client/pull/29) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Signal RegisterUserWorker [\#28](https://github.com/Criptext/Android-Email-Client/pull/28) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
-  merge all signal implemtations into a single store  [\#26](https://github.com/Criptext/Android-Email-Client/pull/26) ([not-gabriel](https://github.com/not-gabriel))
- Implement Signed PreKey store with Room [\#25](https://github.com/Criptext/Android-Email-Client/pull/25) ([not-gabriel](https://github.com/not-gabriel))
- Implement Identity Key Store with Room [\#24](https://github.com/Criptext/Android-Email-Client/pull/24) ([not-gabriel](https://github.com/not-gabriel))
-  Implement PreKeyStore with Room  [\#23](https://github.com/Criptext/Android-Email-Client/pull/23) ([not-gabriel](https://github.com/not-gabriel))
- Create Session Store [\#22](https://github.com/Criptext/Android-Email-Client/pull/22) ([not-gabriel](https://github.com/not-gabriel))
- Merge sign up and SignIn [\#21](https://github.com/Criptext/Android-Email-Client/pull/21) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Signupcontrollertest [\#20](https://github.com/Criptext/Android-Email-Client/pull/20) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Implement composer scene [\#19](https://github.com/Criptext/Android-Email-Client/pull/19) ([not-gabriel](https://github.com/not-gabriel))
- Create composer package [\#18](https://github.com/Criptext/Android-Email-Client/pull/18) ([not-gabriel](https://github.com/not-gabriel))
- Basic dataSource SignUp [\#17](https://github.com/Criptext/Android-Email-Client/pull/17) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- login animations [\#16](https://github.com/Criptext/Android-Email-Client/pull/16) ([danieltigse](https://github.com/danieltigse))
- minor improvements to `FeedControllerTest` [\#15](https://github.com/Criptext/Android-Email-Client/pull/15) ([not-gabriel](https://github.com/not-gabriel))
- Login and Signup basic layout [\#14](https://github.com/Criptext/Android-Email-Client/pull/14) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- implement FeedDataSource [\#13](https://github.com/Criptext/Android-Email-Client/pull/13) ([not-gabriel](https://github.com/not-gabriel))
- Pass DrawerFeedView in FeedController constructor [\#12](https://github.com/Criptext/Android-Email-Client/pull/12) ([not-gabriel](https://github.com/not-gabriel))
- Manage global state and navigation in BaseActivity [\#11](https://github.com/Criptext/Android-Email-Client/pull/11) ([not-gabriel](https://github.com/not-gabriel))
- Create Base Activity [\#9](https://github.com/Criptext/Android-Email-Client/pull/9) ([not-gabriel](https://github.com/not-gabriel))
- Create interface VirtualList [\#7](https://github.com/Criptext/Android-Email-Client/pull/7) ([not-gabriel](https://github.com/not-gabriel))
- SearchView, Activity Feed and Drawer Menus [\#6](https://github.com/Criptext/Android-Email-Client/pull/6) ([danieltigse](https://github.com/danieltigse))
-  improved mailbox UI, move email to trash  [\#5](https://github.com/Criptext/Android-Email-Client/pull/5) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- Added missing models [\#4](https://github.com/Criptext/Android-Email-Client/pull/4) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- NavegationView & Drawers [\#3](https://github.com/Criptext/Android-Email-Client/pull/3) ([danieltigse](https://github.com/danieltigse))
- Added MultiMode handling in mailbox. [\#2](https://github.com/Criptext/Android-Email-Client/pull/2) ([PinkFLoyd92](https://github.com/PinkFLoyd92))
- First Sample of Mailbox. Added Email and Label model, rendering maiibox items… [\#1](https://github.com/Criptext/Android-Email-Client/pull/1) ([PinkFLoyd92](https://github.com/PinkFLoyd92))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*