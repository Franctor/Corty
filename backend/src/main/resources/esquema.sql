
    create table activation_tokens (
        used bit not null,
        expires_at datetime(6) not null,
        id_token bigint not null auto_increment,
        id_user bigint not null,
        token varchar(36) not null,
        primary key (id_token)
    ) engine=InnoDB;

    create table authorities (
        id_authority bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id_authority)
    ) engine=InnoDB;

    create table bookings (
        date date not null,
        end_time time(0) not null,
        fully_paid bit not null,
        split_payment bit not null,
        start_time time(0) not null,
        total_price decimal(8,2) not null,
        created_at datetime(6),
        id_booking bigint not null auto_increment,
        id_court bigint not null,
        id_user bigint not null,
        notes varchar(100),
        payment_id varchar(255),
        result varchar(255),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','PENDING_PAYMENT') not null,
        booking_type enum ('PRIVATE','PUBLIC') not null,
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        primary key (id_booking)
    ) engine=InnoDB;

    create table cities (
        id_city bigint not null auto_increment,
        code_city varchar(255) not null,
        label varchar(255) not null,
        province_code varchar(255),
        primary key (id_city)
    ) engine=InnoDB;

    create table club_balance_entries (
        amount decimal(10,2) not null,
        created_at datetime(6) not null,
        id_booking bigint,
        id_club bigint not null,
        id_entry bigint not null auto_increment,
        description varchar(200),
        reason enum ('OWNER_LAST_MINUTE_CANCEL','OWNER_LATE_CANCEL','PARTICIPANT_LAST_MINUTE_CANCEL','PARTICIPANT_LATE_CANCEL') not null,
        primary key (id_entry)
    ) engine=InnoDB;

    create table club_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_club bigint not null,
        id_club_review bigint not null auto_increment,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_club_review)
    ) engine=InnoDB;

    create table clubs (
        geo_lat decimal(10,8),
        geo_long decimal(11,8),
        created_at datetime(6) not null,
        id_city bigint not null,
        id_club bigint not null auto_increment,
        id_organization bigint not null,
        nif varchar(9) not null,
        phone varchar(20) not null,
        name varchar(50) not null,
        address varchar(100) not null,
        contact_email varchar(100) not null,
        logo_url varchar(255),
        description tinytext not null,
        primary key (id_club)
    ) engine=InnoDB;

    create table conversation_participants (
        id_conversation bigint not null,
        id_user bigint not null,
        primary key (id_conversation, id_user)
    ) engine=InnoDB;

    create table conversations (
        created_at datetime(6) not null,
        id_conversation bigint not null auto_increment,
        last_message_at datetime(6),
        primary key (id_conversation)
    ) engine=InnoDB;

    create table court_blocks (
        block_date date not null,
        end_time time(0) not null,
        start_time time(0) not null,
        id_court bigint not null,
        id_court_block bigint not null auto_increment,
        reason varchar(200),
        primary key (id_court_block)
    ) engine=InnoDB;

    create table court_schedules (
        close_time time(0),
        is_closed bit not null,
        open_time time(0),
        id_court bigint not null,
        id_court_schedule bigint not null auto_increment,
        day_of_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_court_schedule)
    ) engine=InnoDB;

    create table courts (
        active bit not null,
        has_lighting bit not null,
        is_covered bit not null,
        price_per_hour decimal(8,2) not null,
        slot_duration_minutes integer not null,
        use_club_schedule bit not null,
        id_club bigint not null,
        id_court bigint not null auto_increment,
        id_sport bigint not null,
        id_surface bigint,
        name varchar(50) not null,
        image_url varchar(255),
        primary key (id_court)
    ) engine=InnoDB;

    create table friendships (
        created_at datetime(6) not null,
        id_friendship bigint not null auto_increment,
        id_recipient bigint not null,
        id_requester bigint not null,
        updated_at datetime(6),
        status enum ('ACCEPTED','BLOCKED','DECLINED','PENDING') not null,
        primary key (id_friendship)
    ) engine=InnoDB;

    create table horaries_clubs (
        close_time time(0) not null,
        is_closed bit not null,
        open_time time(0) not null,
        id_club bigint not null,
        id_horary_club bigint not null auto_increment,
        day_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_horary_club)
    ) engine=InnoDB;

    create table join_requests (
        created_at datetime(6),
        id_booking bigint not null,
        id_join_request bigint not null auto_increment,
        id_player bigint not null,
        status enum ('ACCEPTED','PENDING','REJECTED') not null,
        primary key (id_join_request)
    ) engine=InnoDB;

    create table messages (
        is_read bit,
        id_conversation bigint not null,
        id_message bigint not null auto_increment,
        id_sender bigint,
        sent_at datetime(6) not null,
        content TEXT not null,
        primary key (id_message)
    ) engine=InnoDB;

    create table notifications (
        is_read bit not null,
        created_at datetime(6),
        id_notification bigint not null auto_increment,
        id_user bigint not null,
        reference_id bigint,
        message varchar(500) not null,
        title varchar(255) not null,
        type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT'),
        primary key (id_notification)
    ) engine=InnoDB;

    create table organizations (
        id_city bigint,
        id_organization bigint not null auto_increment,
        id_user bigint,
        cif varchar(9),
        business_name varchar(100),
        primary key (id_organization)
    ) engine=InnoDB;

    create table player_bookings (
        has_paid bit not null,
        is_confirmed bit not null,
        is_winner bit not null,
        paid_amount decimal(8,2),
        split_price decimal(8,2) not null,
        id_booking bigint not null,
        id_player bigint not null,
        id_player_booking bigint not null auto_increment,
        paid_at datetime(6),
        payment_id varchar(255),
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        team enum ('A','B','NONE') not null,
        primary key (id_player_booking)
    ) engine=InnoDB;

    create table player_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_player_review bigint not null auto_increment,
        id_rated_player bigint not null,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_player_review)
    ) engine=InnoDB;

    create table player_sport_positions (
        id_player_sport bigint not null,
        id_position bigint not null
    ) engine=InnoDB;

    create table player_sports (
        level float(53) not null,
        losses integer not null check ((losses>=0)),
        played_matches integer not null check ((played_matches>=0)),
        wins integer not null check ((wins>=0)),
        id_player bigint not null,
        id_player_sport bigint not null auto_increment,
        id_sport bigint not null,
        last_level_change datetime(6),
        dominant_side enum ('AMBIDEXTROUS','LEFT','NOT_APPLICABLE','RIGHT'),
        primary key (id_player_sport)
    ) engine=InnoDB;

    create table players (
        birth_date date,
        karma integer not null check ((karma<=100) and (karma>=0)),
        profile_complete bit not null,
        public_profile bit not null,
        id_city bigint,
        id_player bigint not null auto_increment,
        id_user bigint,
        phone varchar(20),
        name varchar(50),
        surname varchar(50),
        avatar_url varchar(255),
        biography varchar(255),
        default_payment_method_id varchar(255),
        stripe_customer_id varchar(255),
        gender enum ('FEMALE','MALE','OTHER'),
        primary key (id_player)
    ) engine=InnoDB;

    create table provinces (
        label varchar(255) not null,
        province_code varchar(255) not null,
        region_code varchar(255),
        primary key (province_code)
    ) engine=InnoDB;

    create table regions (
        label varchar(255) not null,
        region_code varchar(255) not null,
        primary key (region_code)
    ) engine=InnoDB;

    create table roles (
        id_role bigint not null auto_increment,
        name varchar(50) not null,
        primary key (id_role)
    ) engine=InnoDB;

    create table sport_positions (
        id_main_position bigint not null,
        id_sport bigint not null,
        id_sport_position bigint not null auto_increment,
        description varchar(50),
        name varchar(50) not null,
        primary key (id_sport_position)
    ) engine=InnoDB;

    create table sports (
        is_team_sport bit not null,
        players_per_match integer not null check ((players_per_match>=1)),
        players_per_team integer not null check ((players_per_team>=0)),
        color varchar(7),
        id_sport bigint not null auto_increment,
        name varchar(50) not null,
        icon_url varchar(255) not null,
        primary key (id_sport)
    ) engine=InnoDB;

    create table surfaces (
        id_surface bigint not null auto_increment,
        name varchar(50) not null,
        description varchar(100),
        icon_url varchar(255),
        primary key (id_surface)
    ) engine=InnoDB;

    create table user_authorities (
        id_authority bigint not null,
        id_user bigint not null,
        primary key (id_authority, id_user)
    ) engine=InnoDB;

    create table user_notification_prefs (
        enabled bit not null,
        id bigint not null auto_increment,
        id_user bigint not null,
        notification_type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table users (
        enabled bit not null,
        locked bit not null,
        creation_date datetime(6) not null,
        expiry_date datetime(6),
        id_role bigint,
        id_user bigint not null auto_increment,
        username varchar(50) not null,
        email varchar(100) not null,
        fcm_token varchar(512),
        password varchar(255) not null,
        primary key (id_user)
    ) engine=InnoDB;

    alter table activation_tokens 
       add constraint UK7gk0sgbu5kmofr1n7xei6k26o unique (id_user);

    alter table activation_tokens 
       add constraint UK5jny0xpou62bqdjhkbw1c0qxd unique (token);

    alter table authorities 
       add constraint UKnb3atvjf9ov5d0egnuk47o5e unique (name);

    alter table clubs 
       add constraint UKk8ri1ow2p79fqvsui7cylak45 unique (nif);

    alter table clubs 
       add constraint UKiovve4w9tgapllooqjhju80we unique (phone);

    alter table clubs 
       add constraint UKq5c0dvdxphxiksvc50hsh53v1 unique (contact_email);

    alter table friendships 
       add constraint UKjujkxdk8yymq29aupfs7g6etb unique (id_requester, id_recipient);

    alter table join_requests 
       add constraint UK4wg0p7nntehlo3p5yevyg3jls unique (id_booking, id_player);

    alter table organizations 
       add constraint UK46gspyum9s50xlg35l9tycurv unique (id_user);

    alter table organizations 
       add constraint UKjp9bf801x0g3g9q7dbdo3blen unique (cif);

    alter table organizations 
       add constraint UKm913d441k0pvuje2fuvgkyfhk unique (business_name);

    alter table player_bookings 
       add constraint UKhm94gx41qn5182avjkgd83lea unique (id_player, id_booking);

    alter table player_sports 
       add constraint UKos7piub52un3pqbmdtqkdne3u unique (id_player, id_sport);

    alter table players 
       add constraint UKa2lbekmluj7xjhfaprdoxvsiw unique (id_user);

    alter table players 
       add constraint UKp4ehqrxqot49ecrj90709botp unique (phone);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table sports 
       add constraint UKtj61or3k005spbrx0lgjpwtde unique (name);

    alter table surfaces 
       add constraint UK7wj431rn8tcjj46jmhp7xkr5a unique (name);

    alter table user_notification_prefs 
       add constraint UK3vdq2nqineo1t2co1rrq6av5h unique (id_user, notification_type);

    alter table users 
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table activation_tokens 
       add constraint FKddwuel6p38uhlh8f6tnyiv54q 
       foreign key (id_user) 
       references users (id_user);

    alter table bookings 
       add constraint FK9sj0his787vx27qad5bdw71pf 
       foreign key (id_court) 
       references courts (id_court);

    alter table bookings 
       add constraint FK7n19id7tbioal42r2i75bg4fx 
       foreign key (id_user) 
       references users (id_user);

    alter table cities 
       add constraint FKq1kpald6bqow9ob7rx9i8s6cg 
       foreign key (province_code) 
       references provinces (province_code);

    alter table club_balance_entries 
       add constraint FKp3sd6ihvp7t9gctcg9h0rp3uf 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table club_balance_entries 
       add constraint FKtcdff686nayflmxn4i913l5nk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKtq0sfw4yrwaa8m0chasctlvgk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKl1jb7yy69jkjfqy8nv9dw5leh 
       foreign key (id_rater) 
       references players (id_player);

    alter table clubs 
       add constraint FK6ha6q1nc386680wfsk11oje9t 
       foreign key (id_city) 
       references cities (id_city);

    alter table clubs 
       add constraint FKnd9y0cqoyubm5mirbgyth751v 
       foreign key (id_organization) 
       references organizations (id_organization);

    alter table conversation_participants 
       add constraint FK6o66i3oqe1w2svngu7n7kbf2p 
       foreign key (id_user) 
       references users (id_user);

    alter table conversation_participants 
       add constraint FKs1pe3fkv8abeniidi8ouka3lc 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table court_blocks 
       add constraint FKejge1al02mtd3e9u5n3godyid 
       foreign key (id_court) 
       references courts (id_court);

    alter table court_schedules 
       add constraint FKo9plcchg8m9khprg6p7ty34du 
       foreign key (id_court) 
       references courts (id_court);

    alter table courts 
       add constraint FKd9g5muaccfvellut8vsix22tx 
       foreign key (id_club) 
       references clubs (id_club);

    alter table courts 
       add constraint FKdn4q2spas9l49sll0elp19r9u 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table courts 
       add constraint FKmlnu6o52oep2bjevvfd1ltkqo 
       foreign key (id_surface) 
       references surfaces (id_surface);

    alter table friendships 
       add constraint FKtc36fp7369o6pling9jv696pt 
       foreign key (id_recipient) 
       references users (id_user);

    alter table friendships 
       add constraint FKt1hbamgoeh8j6lwbp7j1ew6vs 
       foreign key (id_requester) 
       references users (id_user);

    alter table horaries_clubs 
       add constraint FKc8ygb50884b2cdjcrtnbxopb3 
       foreign key (id_club) 
       references clubs (id_club);

    alter table join_requests 
       add constraint FKetw4eryumf2w0vyoei5sricgc 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table join_requests 
       add constraint FKg0vc3di2oenv0afbq59kpgd8p 
       foreign key (id_player) 
       references players (id_player);

    alter table messages 
       add constraint FK15pncav1yft13vh4ir6qt6o4y 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table messages 
       add constraint FK3nwahn9plcioqsad6fbidro7m 
       foreign key (id_sender) 
       references users (id_user);

    alter table notifications 
       add constraint FKhrebgqe9mgp6x2erxipscbxpi 
       foreign key (id_user) 
       references users (id_user);

    alter table organizations 
       add constraint FKol09qh7vnablxl7yk9450u9lq 
       foreign key (id_city) 
       references cities (id_city);

    alter table organizations 
       add constraint FK31chp6t6n52q5fi15m7hskv89 
       foreign key (id_user) 
       references users (id_user);

    alter table player_bookings 
       add constraint FK1ucnm2ndkpbnmjngqxed75y43 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table player_bookings 
       add constraint FK558mxgubfiogg01enka6e1prs 
       foreign key (id_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FKgvmbthbpmk1hbpyuajls0b26m 
       foreign key (id_rated_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FK3t7777djyqsmv3ilxvwata59k 
       foreign key (id_rater) 
       references players (id_player);

    alter table player_sport_positions 
       add constraint FKbxcxspgy2tgesaq2n2wp4r4j9 
       foreign key (id_position) 
       references sport_positions (id_sport_position);

    alter table player_sport_positions 
       add constraint FKnpbx3got94pkk1uhl1lpiqyqr 
       foreign key (id_player_sport) 
       references player_sports (id_player_sport);

    alter table player_sports 
       add constraint FKb77t5mb4i16s4jgrh7n11m70k 
       foreign key (id_player) 
       references players (id_player);

    alter table player_sports 
       add constraint FKlqjgg7qepn7vsekd2l6mh8gpw 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table players 
       add constraint FKfs56gdu99u8fysp3i2h18s4cp 
       foreign key (id_city) 
       references cities (id_city);

    alter table players 
       add constraint FK851yg4yct646kgu95cq4glw2u 
       foreign key (id_user) 
       references users (id_user);

    alter table provinces 
       add constraint FKlnwh9ppis015see8jinynepxn 
       foreign key (region_code) 
       references regions (region_code);

    alter table sport_positions 
       add constraint FK1fvo33flyaqtsogd15km4bepj 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table user_authorities 
       add constraint FKefqce5pe254q5hym7rgii9kuj 
       foreign key (id_authority) 
       references authorities (id_authority);

    alter table user_authorities 
       add constraint FKdh0uxx0dxjh34tgxrdvcv48c3 
       foreign key (id_user) 
       references users (id_user);

    alter table user_notification_prefs 
       add constraint FKolef4elu5g9bx9hwmloldifp2 
       foreign key (id_user) 
       references users (id_user);

    alter table users 
       add constraint FKt92dgi4412ywy3u8tm9jwdya5 
       foreign key (id_role) 
       references roles (id_role);

    create table activation_tokens (
        used bit not null,
        expires_at datetime(6) not null,
        id_token bigint not null auto_increment,
        id_user bigint not null,
        token varchar(36) not null,
        primary key (id_token)
    ) engine=InnoDB;

    create table authorities (
        id_authority bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id_authority)
    ) engine=InnoDB;

    create table bookings (
        date date not null,
        end_time time(0) not null,
        fully_paid bit not null,
        split_payment bit not null,
        start_time time(0) not null,
        total_price decimal(8,2) not null,
        created_at datetime(6),
        id_booking bigint not null auto_increment,
        id_court bigint not null,
        id_user bigint not null,
        notes varchar(100),
        payment_id varchar(255),
        result varchar(255),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','PENDING_PAYMENT') not null,
        booking_type enum ('PRIVATE','PUBLIC') not null,
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        primary key (id_booking)
    ) engine=InnoDB;

    create table cities (
        id_city bigint not null auto_increment,
        code_city varchar(255) not null,
        label varchar(255) not null,
        province_code varchar(255),
        primary key (id_city)
    ) engine=InnoDB;

    create table club_balance_entries (
        amount decimal(10,2) not null,
        created_at datetime(6) not null,
        id_booking bigint,
        id_club bigint not null,
        id_entry bigint not null auto_increment,
        description varchar(200),
        reason enum ('OWNER_LAST_MINUTE_CANCEL','OWNER_LATE_CANCEL','PARTICIPANT_LAST_MINUTE_CANCEL','PARTICIPANT_LATE_CANCEL') not null,
        primary key (id_entry)
    ) engine=InnoDB;

    create table club_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_club bigint not null,
        id_club_review bigint not null auto_increment,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_club_review)
    ) engine=InnoDB;

    create table clubs (
        geo_lat decimal(10,8),
        geo_long decimal(11,8),
        created_at datetime(6) not null,
        id_city bigint not null,
        id_club bigint not null auto_increment,
        id_organization bigint not null,
        nif varchar(9) not null,
        phone varchar(20) not null,
        name varchar(50) not null,
        address varchar(100) not null,
        contact_email varchar(100) not null,
        logo_url varchar(255),
        description tinytext not null,
        primary key (id_club)
    ) engine=InnoDB;

    create table conversation_participants (
        id_conversation bigint not null,
        id_user bigint not null,
        primary key (id_conversation, id_user)
    ) engine=InnoDB;

    create table conversations (
        created_at datetime(6) not null,
        id_conversation bigint not null auto_increment,
        last_message_at datetime(6),
        primary key (id_conversation)
    ) engine=InnoDB;

    create table court_blocks (
        block_date date not null,
        end_time time(0) not null,
        start_time time(0) not null,
        id_court bigint not null,
        id_court_block bigint not null auto_increment,
        reason varchar(200),
        primary key (id_court_block)
    ) engine=InnoDB;

    create table court_schedules (
        close_time time(0),
        is_closed bit not null,
        open_time time(0),
        id_court bigint not null,
        id_court_schedule bigint not null auto_increment,
        day_of_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_court_schedule)
    ) engine=InnoDB;

    create table courts (
        active bit not null,
        has_lighting bit not null,
        is_covered bit not null,
        price_per_hour decimal(8,2) not null,
        slot_duration_minutes integer not null,
        use_club_schedule bit not null,
        id_club bigint not null,
        id_court bigint not null auto_increment,
        id_sport bigint not null,
        id_surface bigint,
        name varchar(50) not null,
        image_url varchar(255),
        primary key (id_court)
    ) engine=InnoDB;

    create table friendships (
        created_at datetime(6) not null,
        id_friendship bigint not null auto_increment,
        id_recipient bigint not null,
        id_requester bigint not null,
        updated_at datetime(6),
        status enum ('ACCEPTED','BLOCKED','DECLINED','PENDING') not null,
        primary key (id_friendship)
    ) engine=InnoDB;

    create table horaries_clubs (
        close_time time(0) not null,
        is_closed bit not null,
        open_time time(0) not null,
        id_club bigint not null,
        id_horary_club bigint not null auto_increment,
        day_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_horary_club)
    ) engine=InnoDB;

    create table join_requests (
        created_at datetime(6),
        id_booking bigint not null,
        id_join_request bigint not null auto_increment,
        id_player bigint not null,
        status enum ('ACCEPTED','PENDING','REJECTED') not null,
        primary key (id_join_request)
    ) engine=InnoDB;

    create table messages (
        is_read bit,
        id_conversation bigint not null,
        id_message bigint not null auto_increment,
        id_sender bigint,
        sent_at datetime(6) not null,
        content TEXT not null,
        primary key (id_message)
    ) engine=InnoDB;

    create table notifications (
        is_read bit not null,
        created_at datetime(6),
        id_notification bigint not null auto_increment,
        id_user bigint not null,
        reference_id bigint,
        message varchar(500) not null,
        title varchar(255) not null,
        type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT'),
        primary key (id_notification)
    ) engine=InnoDB;

    create table organizations (
        id_city bigint,
        id_organization bigint not null auto_increment,
        id_user bigint,
        cif varchar(9),
        business_name varchar(100),
        primary key (id_organization)
    ) engine=InnoDB;

    create table player_bookings (
        has_paid bit not null,
        is_confirmed bit not null,
        is_winner bit not null,
        paid_amount decimal(8,2),
        split_price decimal(8,2) not null,
        id_booking bigint not null,
        id_player bigint not null,
        id_player_booking bigint not null auto_increment,
        paid_at datetime(6),
        payment_id varchar(255),
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        team enum ('A','B','NONE') not null,
        primary key (id_player_booking)
    ) engine=InnoDB;

    create table player_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_player_review bigint not null auto_increment,
        id_rated_player bigint not null,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_player_review)
    ) engine=InnoDB;

    create table player_sport_positions (
        id_player_sport bigint not null,
        id_position bigint not null
    ) engine=InnoDB;

    create table player_sports (
        level float(53) not null,
        losses integer not null check ((losses>=0)),
        played_matches integer not null check ((played_matches>=0)),
        wins integer not null check ((wins>=0)),
        id_player bigint not null,
        id_player_sport bigint not null auto_increment,
        id_sport bigint not null,
        last_level_change datetime(6),
        dominant_side enum ('AMBIDEXTROUS','LEFT','NOT_APPLICABLE','RIGHT'),
        primary key (id_player_sport)
    ) engine=InnoDB;

    create table players (
        birth_date date,
        karma integer not null check ((karma<=100) and (karma>=0)),
        profile_complete bit not null,
        public_profile bit not null,
        id_city bigint,
        id_player bigint not null auto_increment,
        id_user bigint,
        phone varchar(20),
        name varchar(50),
        surname varchar(50),
        avatar_url varchar(255),
        biography varchar(255),
        default_payment_method_id varchar(255),
        stripe_customer_id varchar(255),
        gender enum ('FEMALE','MALE','OTHER'),
        primary key (id_player)
    ) engine=InnoDB;

    create table provinces (
        label varchar(255) not null,
        province_code varchar(255) not null,
        region_code varchar(255),
        primary key (province_code)
    ) engine=InnoDB;

    create table regions (
        label varchar(255) not null,
        region_code varchar(255) not null,
        primary key (region_code)
    ) engine=InnoDB;

    create table roles (
        id_role bigint not null auto_increment,
        name varchar(50) not null,
        primary key (id_role)
    ) engine=InnoDB;

    create table sport_positions (
        id_main_position bigint not null,
        id_sport bigint not null,
        id_sport_position bigint not null auto_increment,
        description varchar(50),
        name varchar(50) not null,
        primary key (id_sport_position)
    ) engine=InnoDB;

    create table sports (
        is_team_sport bit not null,
        players_per_match integer not null check ((players_per_match>=1)),
        players_per_team integer not null check ((players_per_team>=0)),
        color varchar(7),
        id_sport bigint not null auto_increment,
        name varchar(50) not null,
        icon_url varchar(255) not null,
        primary key (id_sport)
    ) engine=InnoDB;

    create table surfaces (
        id_surface bigint not null auto_increment,
        name varchar(50) not null,
        description varchar(100),
        icon_url varchar(255),
        primary key (id_surface)
    ) engine=InnoDB;

    create table user_authorities (
        id_authority bigint not null,
        id_user bigint not null,
        primary key (id_authority, id_user)
    ) engine=InnoDB;

    create table user_notification_prefs (
        enabled bit not null,
        id bigint not null auto_increment,
        id_user bigint not null,
        notification_type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table users (
        enabled bit not null,
        locked bit not null,
        creation_date datetime(6) not null,
        expiry_date datetime(6),
        id_role bigint,
        id_user bigint not null auto_increment,
        username varchar(50) not null,
        email varchar(100) not null,
        fcm_token varchar(512),
        password varchar(255) not null,
        primary key (id_user)
    ) engine=InnoDB;

    alter table activation_tokens 
       add constraint UK7gk0sgbu5kmofr1n7xei6k26o unique (id_user);

    alter table activation_tokens 
       add constraint UK5jny0xpou62bqdjhkbw1c0qxd unique (token);

    alter table authorities 
       add constraint UKnb3atvjf9ov5d0egnuk47o5e unique (name);

    alter table clubs 
       add constraint UKk8ri1ow2p79fqvsui7cylak45 unique (nif);

    alter table clubs 
       add constraint UKiovve4w9tgapllooqjhju80we unique (phone);

    alter table clubs 
       add constraint UKq5c0dvdxphxiksvc50hsh53v1 unique (contact_email);

    alter table friendships 
       add constraint UKjujkxdk8yymq29aupfs7g6etb unique (id_requester, id_recipient);

    alter table join_requests 
       add constraint UK4wg0p7nntehlo3p5yevyg3jls unique (id_booking, id_player);

    alter table organizations 
       add constraint UK46gspyum9s50xlg35l9tycurv unique (id_user);

    alter table organizations 
       add constraint UKjp9bf801x0g3g9q7dbdo3blen unique (cif);

    alter table organizations 
       add constraint UKm913d441k0pvuje2fuvgkyfhk unique (business_name);

    alter table player_bookings 
       add constraint UKhm94gx41qn5182avjkgd83lea unique (id_player, id_booking);

    alter table player_sports 
       add constraint UKos7piub52un3pqbmdtqkdne3u unique (id_player, id_sport);

    alter table players 
       add constraint UKa2lbekmluj7xjhfaprdoxvsiw unique (id_user);

    alter table players 
       add constraint UKp4ehqrxqot49ecrj90709botp unique (phone);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table sports 
       add constraint UKtj61or3k005spbrx0lgjpwtde unique (name);

    alter table surfaces 
       add constraint UK7wj431rn8tcjj46jmhp7xkr5a unique (name);

    alter table user_notification_prefs 
       add constraint UK3vdq2nqineo1t2co1rrq6av5h unique (id_user, notification_type);

    alter table users 
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table activation_tokens 
       add constraint FKddwuel6p38uhlh8f6tnyiv54q 
       foreign key (id_user) 
       references users (id_user);

    alter table bookings 
       add constraint FK9sj0his787vx27qad5bdw71pf 
       foreign key (id_court) 
       references courts (id_court);

    alter table bookings 
       add constraint FK7n19id7tbioal42r2i75bg4fx 
       foreign key (id_user) 
       references users (id_user);

    alter table cities 
       add constraint FKq1kpald6bqow9ob7rx9i8s6cg 
       foreign key (province_code) 
       references provinces (province_code);

    alter table club_balance_entries 
       add constraint FKp3sd6ihvp7t9gctcg9h0rp3uf 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table club_balance_entries 
       add constraint FKtcdff686nayflmxn4i913l5nk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKtq0sfw4yrwaa8m0chasctlvgk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKl1jb7yy69jkjfqy8nv9dw5leh 
       foreign key (id_rater) 
       references players (id_player);

    alter table clubs 
       add constraint FK6ha6q1nc386680wfsk11oje9t 
       foreign key (id_city) 
       references cities (id_city);

    alter table clubs 
       add constraint FKnd9y0cqoyubm5mirbgyth751v 
       foreign key (id_organization) 
       references organizations (id_organization);

    alter table conversation_participants 
       add constraint FK6o66i3oqe1w2svngu7n7kbf2p 
       foreign key (id_user) 
       references users (id_user);

    alter table conversation_participants 
       add constraint FKs1pe3fkv8abeniidi8ouka3lc 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table court_blocks 
       add constraint FKejge1al02mtd3e9u5n3godyid 
       foreign key (id_court) 
       references courts (id_court);

    alter table court_schedules 
       add constraint FKo9plcchg8m9khprg6p7ty34du 
       foreign key (id_court) 
       references courts (id_court);

    alter table courts 
       add constraint FKd9g5muaccfvellut8vsix22tx 
       foreign key (id_club) 
       references clubs (id_club);

    alter table courts 
       add constraint FKdn4q2spas9l49sll0elp19r9u 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table courts 
       add constraint FKmlnu6o52oep2bjevvfd1ltkqo 
       foreign key (id_surface) 
       references surfaces (id_surface);

    alter table friendships 
       add constraint FKtc36fp7369o6pling9jv696pt 
       foreign key (id_recipient) 
       references users (id_user);

    alter table friendships 
       add constraint FKt1hbamgoeh8j6lwbp7j1ew6vs 
       foreign key (id_requester) 
       references users (id_user);

    alter table horaries_clubs 
       add constraint FKc8ygb50884b2cdjcrtnbxopb3 
       foreign key (id_club) 
       references clubs (id_club);

    alter table join_requests 
       add constraint FKetw4eryumf2w0vyoei5sricgc 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table join_requests 
       add constraint FKg0vc3di2oenv0afbq59kpgd8p 
       foreign key (id_player) 
       references players (id_player);

    alter table messages 
       add constraint FK15pncav1yft13vh4ir6qt6o4y 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table messages 
       add constraint FK3nwahn9plcioqsad6fbidro7m 
       foreign key (id_sender) 
       references users (id_user);

    alter table notifications 
       add constraint FKhrebgqe9mgp6x2erxipscbxpi 
       foreign key (id_user) 
       references users (id_user);

    alter table organizations 
       add constraint FKol09qh7vnablxl7yk9450u9lq 
       foreign key (id_city) 
       references cities (id_city);

    alter table organizations 
       add constraint FK31chp6t6n52q5fi15m7hskv89 
       foreign key (id_user) 
       references users (id_user);

    alter table player_bookings 
       add constraint FK1ucnm2ndkpbnmjngqxed75y43 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table player_bookings 
       add constraint FK558mxgubfiogg01enka6e1prs 
       foreign key (id_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FKgvmbthbpmk1hbpyuajls0b26m 
       foreign key (id_rated_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FK3t7777djyqsmv3ilxvwata59k 
       foreign key (id_rater) 
       references players (id_player);

    alter table player_sport_positions 
       add constraint FKbxcxspgy2tgesaq2n2wp4r4j9 
       foreign key (id_position) 
       references sport_positions (id_sport_position);

    alter table player_sport_positions 
       add constraint FKnpbx3got94pkk1uhl1lpiqyqr 
       foreign key (id_player_sport) 
       references player_sports (id_player_sport);

    alter table player_sports 
       add constraint FKb77t5mb4i16s4jgrh7n11m70k 
       foreign key (id_player) 
       references players (id_player);

    alter table player_sports 
       add constraint FKlqjgg7qepn7vsekd2l6mh8gpw 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table players 
       add constraint FKfs56gdu99u8fysp3i2h18s4cp 
       foreign key (id_city) 
       references cities (id_city);

    alter table players 
       add constraint FK851yg4yct646kgu95cq4glw2u 
       foreign key (id_user) 
       references users (id_user);

    alter table provinces 
       add constraint FKlnwh9ppis015see8jinynepxn 
       foreign key (region_code) 
       references regions (region_code);

    alter table sport_positions 
       add constraint FK1fvo33flyaqtsogd15km4bepj 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table user_authorities 
       add constraint FKefqce5pe254q5hym7rgii9kuj 
       foreign key (id_authority) 
       references authorities (id_authority);

    alter table user_authorities 
       add constraint FKdh0uxx0dxjh34tgxrdvcv48c3 
       foreign key (id_user) 
       references users (id_user);

    alter table user_notification_prefs 
       add constraint FKolef4elu5g9bx9hwmloldifp2 
       foreign key (id_user) 
       references users (id_user);

    alter table users 
       add constraint FKt92dgi4412ywy3u8tm9jwdya5 
       foreign key (id_role) 
       references roles (id_role);

    create table activation_tokens (
        used bit not null,
        expires_at datetime(6) not null,
        id_token bigint not null auto_increment,
        id_user bigint not null,
        token varchar(36) not null,
        primary key (id_token)
    ) engine=InnoDB;

    create table authorities (
        id_authority bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id_authority)
    ) engine=InnoDB;

    create table bookings (
        date date not null,
        end_time time(0) not null,
        fully_paid bit not null,
        split_payment bit not null,
        start_time time(0) not null,
        total_price decimal(8,2) not null,
        created_at datetime(6),
        id_booking bigint not null auto_increment,
        id_court bigint not null,
        id_user bigint not null,
        notes varchar(100),
        payment_id varchar(255),
        result varchar(255),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','PENDING_PAYMENT') not null,
        booking_type enum ('PRIVATE','PUBLIC') not null,
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        primary key (id_booking)
    ) engine=InnoDB;

    create table cities (
        id_city bigint not null auto_increment,
        code_city varchar(255) not null,
        label varchar(255) not null,
        province_code varchar(255),
        primary key (id_city)
    ) engine=InnoDB;

    create table club_balance_entries (
        amount decimal(10,2) not null,
        created_at datetime(6) not null,
        id_booking bigint,
        id_club bigint not null,
        id_entry bigint not null auto_increment,
        description varchar(200),
        reason enum ('OWNER_LAST_MINUTE_CANCEL','OWNER_LATE_CANCEL','PARTICIPANT_LAST_MINUTE_CANCEL','PARTICIPANT_LATE_CANCEL') not null,
        primary key (id_entry)
    ) engine=InnoDB;

    create table club_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_club bigint not null,
        id_club_review bigint not null auto_increment,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_club_review)
    ) engine=InnoDB;

    create table clubs (
        geo_lat decimal(10,8),
        geo_long decimal(11,8),
        created_at datetime(6) not null,
        id_city bigint not null,
        id_club bigint not null auto_increment,
        id_organization bigint not null,
        nif varchar(9) not null,
        phone varchar(20) not null,
        name varchar(50) not null,
        address varchar(100) not null,
        contact_email varchar(100) not null,
        logo_url varchar(255),
        description tinytext not null,
        primary key (id_club)
    ) engine=InnoDB;

    create table conversation_participants (
        id_conversation bigint not null,
        id_user bigint not null,
        primary key (id_conversation, id_user)
    ) engine=InnoDB;

    create table conversations (
        created_at datetime(6) not null,
        id_conversation bigint not null auto_increment,
        last_message_at datetime(6),
        primary key (id_conversation)
    ) engine=InnoDB;

    create table court_blocks (
        block_date date not null,
        end_time time(0) not null,
        start_time time(0) not null,
        id_court bigint not null,
        id_court_block bigint not null auto_increment,
        reason varchar(200),
        primary key (id_court_block)
    ) engine=InnoDB;

    create table court_schedules (
        close_time time(0),
        is_closed bit not null,
        open_time time(0),
        id_court bigint not null,
        id_court_schedule bigint not null auto_increment,
        day_of_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_court_schedule)
    ) engine=InnoDB;

    create table courts (
        active bit not null,
        has_lighting bit not null,
        is_covered bit not null,
        price_per_hour decimal(8,2) not null,
        slot_duration_minutes integer not null,
        use_club_schedule bit not null,
        id_club bigint not null,
        id_court bigint not null auto_increment,
        id_sport bigint not null,
        id_surface bigint,
        name varchar(50) not null,
        image_url varchar(255),
        primary key (id_court)
    ) engine=InnoDB;

    create table friendships (
        created_at datetime(6) not null,
        id_friendship bigint not null auto_increment,
        id_recipient bigint not null,
        id_requester bigint not null,
        updated_at datetime(6),
        status enum ('ACCEPTED','BLOCKED','DECLINED','PENDING') not null,
        primary key (id_friendship)
    ) engine=InnoDB;

    create table horaries_clubs (
        close_time time(0) not null,
        is_closed bit not null,
        open_time time(0) not null,
        id_club bigint not null,
        id_horary_club bigint not null auto_increment,
        day_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_horary_club)
    ) engine=InnoDB;

    create table join_requests (
        created_at datetime(6),
        id_booking bigint not null,
        id_join_request bigint not null auto_increment,
        id_player bigint not null,
        status enum ('ACCEPTED','PENDING','REJECTED') not null,
        primary key (id_join_request)
    ) engine=InnoDB;

    create table messages (
        is_read bit,
        id_conversation bigint not null,
        id_message bigint not null auto_increment,
        id_sender bigint,
        sent_at datetime(6) not null,
        content TEXT not null,
        primary key (id_message)
    ) engine=InnoDB;

    create table notifications (
        is_read bit not null,
        created_at datetime(6),
        id_notification bigint not null auto_increment,
        id_user bigint not null,
        reference_id bigint,
        message varchar(500) not null,
        title varchar(255) not null,
        type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT'),
        primary key (id_notification)
    ) engine=InnoDB;

    create table organizations (
        id_city bigint,
        id_organization bigint not null auto_increment,
        id_user bigint,
        cif varchar(9),
        business_name varchar(100),
        primary key (id_organization)
    ) engine=InnoDB;

    create table player_bookings (
        has_paid bit not null,
        is_confirmed bit not null,
        is_winner bit not null,
        paid_amount decimal(8,2),
        split_price decimal(8,2) not null,
        id_booking bigint not null,
        id_player bigint not null,
        id_player_booking bigint not null auto_increment,
        paid_at datetime(6),
        payment_id varchar(255),
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        team enum ('A','B','NONE') not null,
        primary key (id_player_booking)
    ) engine=InnoDB;

    create table player_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_player_review bigint not null auto_increment,
        id_rated_player bigint not null,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_player_review)
    ) engine=InnoDB;

    create table player_sport_positions (
        id_player_sport bigint not null,
        id_position bigint not null
    ) engine=InnoDB;

    create table player_sports (
        level float(53) not null,
        losses integer not null check ((losses>=0)),
        played_matches integer not null check ((played_matches>=0)),
        wins integer not null check ((wins>=0)),
        id_player bigint not null,
        id_player_sport bigint not null auto_increment,
        id_sport bigint not null,
        last_level_change datetime(6),
        dominant_side enum ('AMBIDEXTROUS','LEFT','NOT_APPLICABLE','RIGHT'),
        primary key (id_player_sport)
    ) engine=InnoDB;

    create table players (
        birth_date date,
        karma integer not null check ((karma<=100) and (karma>=0)),
        profile_complete bit not null,
        public_profile bit not null,
        id_city bigint,
        id_player bigint not null auto_increment,
        id_user bigint,
        phone varchar(20),
        name varchar(50),
        surname varchar(50),
        avatar_url varchar(255),
        biography varchar(255),
        default_payment_method_id varchar(255),
        stripe_customer_id varchar(255),
        gender enum ('FEMALE','MALE','OTHER'),
        primary key (id_player)
    ) engine=InnoDB;

    create table provinces (
        label varchar(255) not null,
        province_code varchar(255) not null,
        region_code varchar(255),
        primary key (province_code)
    ) engine=InnoDB;

    create table regions (
        label varchar(255) not null,
        region_code varchar(255) not null,
        primary key (region_code)
    ) engine=InnoDB;

    create table roles (
        id_role bigint not null auto_increment,
        name varchar(50) not null,
        primary key (id_role)
    ) engine=InnoDB;

    create table sport_positions (
        id_main_position bigint not null,
        id_sport bigint not null,
        id_sport_position bigint not null auto_increment,
        description varchar(50),
        name varchar(50) not null,
        primary key (id_sport_position)
    ) engine=InnoDB;

    create table sports (
        is_team_sport bit not null,
        players_per_match integer not null check ((players_per_match>=1)),
        players_per_team integer not null check ((players_per_team>=0)),
        color varchar(7),
        id_sport bigint not null auto_increment,
        name varchar(50) not null,
        icon_url varchar(255) not null,
        primary key (id_sport)
    ) engine=InnoDB;

    create table surfaces (
        id_surface bigint not null auto_increment,
        name varchar(50) not null,
        description varchar(100),
        icon_url varchar(255),
        primary key (id_surface)
    ) engine=InnoDB;

    create table user_authorities (
        id_authority bigint not null,
        id_user bigint not null,
        primary key (id_authority, id_user)
    ) engine=InnoDB;

    create table user_notification_prefs (
        enabled bit not null,
        id bigint not null auto_increment,
        id_user bigint not null,
        notification_type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table users (
        enabled bit not null,
        locked bit not null,
        creation_date datetime(6) not null,
        expiry_date datetime(6),
        id_role bigint,
        id_user bigint not null auto_increment,
        username varchar(50) not null,
        email varchar(100) not null,
        fcm_token varchar(512),
        password varchar(255) not null,
        primary key (id_user)
    ) engine=InnoDB;

    alter table activation_tokens 
       add constraint UK7gk0sgbu5kmofr1n7xei6k26o unique (id_user);

    alter table activation_tokens 
       add constraint UK5jny0xpou62bqdjhkbw1c0qxd unique (token);

    alter table authorities 
       add constraint UKnb3atvjf9ov5d0egnuk47o5e unique (name);

    alter table clubs 
       add constraint UKk8ri1ow2p79fqvsui7cylak45 unique (nif);

    alter table clubs 
       add constraint UKiovve4w9tgapllooqjhju80we unique (phone);

    alter table clubs 
       add constraint UKq5c0dvdxphxiksvc50hsh53v1 unique (contact_email);

    alter table friendships 
       add constraint UKjujkxdk8yymq29aupfs7g6etb unique (id_requester, id_recipient);

    alter table join_requests 
       add constraint UK4wg0p7nntehlo3p5yevyg3jls unique (id_booking, id_player);

    alter table organizations 
       add constraint UK46gspyum9s50xlg35l9tycurv unique (id_user);

    alter table organizations 
       add constraint UKjp9bf801x0g3g9q7dbdo3blen unique (cif);

    alter table organizations 
       add constraint UKm913d441k0pvuje2fuvgkyfhk unique (business_name);

    alter table player_bookings 
       add constraint UKhm94gx41qn5182avjkgd83lea unique (id_player, id_booking);

    alter table player_sports 
       add constraint UKos7piub52un3pqbmdtqkdne3u unique (id_player, id_sport);

    alter table players 
       add constraint UKa2lbekmluj7xjhfaprdoxvsiw unique (id_user);

    alter table players 
       add constraint UKp4ehqrxqot49ecrj90709botp unique (phone);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table sports 
       add constraint UKtj61or3k005spbrx0lgjpwtde unique (name);

    alter table surfaces 
       add constraint UK7wj431rn8tcjj46jmhp7xkr5a unique (name);

    alter table user_notification_prefs 
       add constraint UK3vdq2nqineo1t2co1rrq6av5h unique (id_user, notification_type);

    alter table users 
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table activation_tokens 
       add constraint FKddwuel6p38uhlh8f6tnyiv54q 
       foreign key (id_user) 
       references users (id_user);

    alter table bookings 
       add constraint FK9sj0his787vx27qad5bdw71pf 
       foreign key (id_court) 
       references courts (id_court);

    alter table bookings 
       add constraint FK7n19id7tbioal42r2i75bg4fx 
       foreign key (id_user) 
       references users (id_user);

    alter table cities 
       add constraint FKq1kpald6bqow9ob7rx9i8s6cg 
       foreign key (province_code) 
       references provinces (province_code);

    alter table club_balance_entries 
       add constraint FKp3sd6ihvp7t9gctcg9h0rp3uf 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table club_balance_entries 
       add constraint FKtcdff686nayflmxn4i913l5nk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKtq0sfw4yrwaa8m0chasctlvgk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKl1jb7yy69jkjfqy8nv9dw5leh 
       foreign key (id_rater) 
       references players (id_player);

    alter table clubs 
       add constraint FK6ha6q1nc386680wfsk11oje9t 
       foreign key (id_city) 
       references cities (id_city);

    alter table clubs 
       add constraint FKnd9y0cqoyubm5mirbgyth751v 
       foreign key (id_organization) 
       references organizations (id_organization);

    alter table conversation_participants 
       add constraint FK6o66i3oqe1w2svngu7n7kbf2p 
       foreign key (id_user) 
       references users (id_user);

    alter table conversation_participants 
       add constraint FKs1pe3fkv8abeniidi8ouka3lc 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table court_blocks 
       add constraint FKejge1al02mtd3e9u5n3godyid 
       foreign key (id_court) 
       references courts (id_court);

    alter table court_schedules 
       add constraint FKo9plcchg8m9khprg6p7ty34du 
       foreign key (id_court) 
       references courts (id_court);

    alter table courts 
       add constraint FKd9g5muaccfvellut8vsix22tx 
       foreign key (id_club) 
       references clubs (id_club);

    alter table courts 
       add constraint FKdn4q2spas9l49sll0elp19r9u 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table courts 
       add constraint FKmlnu6o52oep2bjevvfd1ltkqo 
       foreign key (id_surface) 
       references surfaces (id_surface);

    alter table friendships 
       add constraint FKtc36fp7369o6pling9jv696pt 
       foreign key (id_recipient) 
       references users (id_user);

    alter table friendships 
       add constraint FKt1hbamgoeh8j6lwbp7j1ew6vs 
       foreign key (id_requester) 
       references users (id_user);

    alter table horaries_clubs 
       add constraint FKc8ygb50884b2cdjcrtnbxopb3 
       foreign key (id_club) 
       references clubs (id_club);

    alter table join_requests 
       add constraint FKetw4eryumf2w0vyoei5sricgc 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table join_requests 
       add constraint FKg0vc3di2oenv0afbq59kpgd8p 
       foreign key (id_player) 
       references players (id_player);

    alter table messages 
       add constraint FK15pncav1yft13vh4ir6qt6o4y 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table messages 
       add constraint FK3nwahn9plcioqsad6fbidro7m 
       foreign key (id_sender) 
       references users (id_user);

    alter table notifications 
       add constraint FKhrebgqe9mgp6x2erxipscbxpi 
       foreign key (id_user) 
       references users (id_user);

    alter table organizations 
       add constraint FKol09qh7vnablxl7yk9450u9lq 
       foreign key (id_city) 
       references cities (id_city);

    alter table organizations 
       add constraint FK31chp6t6n52q5fi15m7hskv89 
       foreign key (id_user) 
       references users (id_user);

    alter table player_bookings 
       add constraint FK1ucnm2ndkpbnmjngqxed75y43 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table player_bookings 
       add constraint FK558mxgubfiogg01enka6e1prs 
       foreign key (id_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FKgvmbthbpmk1hbpyuajls0b26m 
       foreign key (id_rated_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FK3t7777djyqsmv3ilxvwata59k 
       foreign key (id_rater) 
       references players (id_player);

    alter table player_sport_positions 
       add constraint FKbxcxspgy2tgesaq2n2wp4r4j9 
       foreign key (id_position) 
       references sport_positions (id_sport_position);

    alter table player_sport_positions 
       add constraint FKnpbx3got94pkk1uhl1lpiqyqr 
       foreign key (id_player_sport) 
       references player_sports (id_player_sport);

    alter table player_sports 
       add constraint FKb77t5mb4i16s4jgrh7n11m70k 
       foreign key (id_player) 
       references players (id_player);

    alter table player_sports 
       add constraint FKlqjgg7qepn7vsekd2l6mh8gpw 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table players 
       add constraint FKfs56gdu99u8fysp3i2h18s4cp 
       foreign key (id_city) 
       references cities (id_city);

    alter table players 
       add constraint FK851yg4yct646kgu95cq4glw2u 
       foreign key (id_user) 
       references users (id_user);

    alter table provinces 
       add constraint FKlnwh9ppis015see8jinynepxn 
       foreign key (region_code) 
       references regions (region_code);

    alter table sport_positions 
       add constraint FK1fvo33flyaqtsogd15km4bepj 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table user_authorities 
       add constraint FKefqce5pe254q5hym7rgii9kuj 
       foreign key (id_authority) 
       references authorities (id_authority);

    alter table user_authorities 
       add constraint FKdh0uxx0dxjh34tgxrdvcv48c3 
       foreign key (id_user) 
       references users (id_user);

    alter table user_notification_prefs 
       add constraint FKolef4elu5g9bx9hwmloldifp2 
       foreign key (id_user) 
       references users (id_user);

    alter table users 
       add constraint FKt92dgi4412ywy3u8tm9jwdya5 
       foreign key (id_role) 
       references roles (id_role);

    create table activation_tokens (
        used bit not null,
        expires_at datetime(6) not null,
        id_token bigint not null auto_increment,
        id_user bigint not null,
        token varchar(36) not null,
        primary key (id_token)
    ) engine=InnoDB;

    create table authorities (
        id_authority bigint not null auto_increment,
        name varchar(255) not null,
        primary key (id_authority)
    ) engine=InnoDB;

    create table bookings (
        date date not null,
        end_time time(0) not null,
        fully_paid bit not null,
        split_payment bit not null,
        start_time time(0) not null,
        total_price decimal(8,2) not null,
        created_at datetime(6),
        id_booking bigint not null auto_increment,
        id_court bigint not null,
        id_user bigint not null,
        notes varchar(100),
        payment_id varchar(255),
        result varchar(255),
        booking_status enum ('CANCELLED','COMPLETED','CONFIRMED','PENDING_PAYMENT') not null,
        booking_type enum ('PRIVATE','PUBLIC') not null,
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        primary key (id_booking)
    ) engine=InnoDB;

    create table cities (
        id_city bigint not null auto_increment,
        code_city varchar(255) not null,
        label varchar(255) not null,
        province_code varchar(255),
        primary key (id_city)
    ) engine=InnoDB;

    create table club_balance_entries (
        amount decimal(10,2) not null,
        created_at datetime(6) not null,
        id_booking bigint,
        id_club bigint not null,
        id_entry bigint not null auto_increment,
        description varchar(200),
        reason enum ('OWNER_LAST_MINUTE_CANCEL','OWNER_LATE_CANCEL','PARTICIPANT_LAST_MINUTE_CANCEL','PARTICIPANT_LATE_CANCEL') not null,
        primary key (id_entry)
    ) engine=InnoDB;

    create table club_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_club bigint not null,
        id_club_review bigint not null auto_increment,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_club_review)
    ) engine=InnoDB;

    create table clubs (
        geo_lat decimal(10,8),
        geo_long decimal(11,8),
        created_at datetime(6) not null,
        id_city bigint not null,
        id_club bigint not null auto_increment,
        id_organization bigint not null,
        nif varchar(9) not null,
        phone varchar(20) not null,
        name varchar(50) not null,
        address varchar(100) not null,
        contact_email varchar(100) not null,
        logo_url varchar(255),
        description tinytext not null,
        primary key (id_club)
    ) engine=InnoDB;

    create table conversation_participants (
        id_conversation bigint not null,
        id_user bigint not null,
        primary key (id_conversation, id_user)
    ) engine=InnoDB;

    create table conversations (
        created_at datetime(6) not null,
        id_conversation bigint not null auto_increment,
        last_message_at datetime(6),
        primary key (id_conversation)
    ) engine=InnoDB;

    create table court_blocks (
        block_date date not null,
        end_time time(0) not null,
        start_time time(0) not null,
        id_court bigint not null,
        id_court_block bigint not null auto_increment,
        reason varchar(200),
        primary key (id_court_block)
    ) engine=InnoDB;

    create table court_schedules (
        close_time time(0),
        is_closed bit not null,
        open_time time(0),
        id_court bigint not null,
        id_court_schedule bigint not null auto_increment,
        day_of_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_court_schedule)
    ) engine=InnoDB;

    create table courts (
        active bit not null,
        has_lighting bit not null,
        is_covered bit not null,
        price_per_hour decimal(8,2) not null,
        slot_duration_minutes integer not null,
        use_club_schedule bit not null,
        id_club bigint not null,
        id_court bigint not null auto_increment,
        id_sport bigint not null,
        id_surface bigint,
        name varchar(50) not null,
        image_url varchar(255),
        primary key (id_court)
    ) engine=InnoDB;

    create table friendships (
        created_at datetime(6) not null,
        id_friendship bigint not null auto_increment,
        id_recipient bigint not null,
        id_requester bigint not null,
        updated_at datetime(6),
        status enum ('ACCEPTED','BLOCKED','DECLINED','PENDING') not null,
        primary key (id_friendship)
    ) engine=InnoDB;

    create table horaries_clubs (
        close_time time(0) not null,
        is_closed bit not null,
        open_time time(0) not null,
        id_club bigint not null,
        id_horary_club bigint not null auto_increment,
        day_week enum ('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') not null,
        primary key (id_horary_club)
    ) engine=InnoDB;

    create table join_requests (
        created_at datetime(6),
        id_booking bigint not null,
        id_join_request bigint not null auto_increment,
        id_player bigint not null,
        status enum ('ACCEPTED','PENDING','REJECTED') not null,
        primary key (id_join_request)
    ) engine=InnoDB;

    create table messages (
        is_read bit,
        id_conversation bigint not null,
        id_message bigint not null auto_increment,
        id_sender bigint,
        sent_at datetime(6) not null,
        content TEXT not null,
        primary key (id_message)
    ) engine=InnoDB;

    create table notifications (
        is_read bit not null,
        created_at datetime(6),
        id_notification bigint not null auto_increment,
        id_user bigint not null,
        reference_id bigint,
        message varchar(500) not null,
        title varchar(255) not null,
        type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT'),
        primary key (id_notification)
    ) engine=InnoDB;

    create table organizations (
        id_city bigint,
        id_organization bigint not null auto_increment,
        id_user bigint,
        cif varchar(9),
        business_name varchar(100),
        primary key (id_organization)
    ) engine=InnoDB;

    create table player_bookings (
        has_paid bit not null,
        is_confirmed bit not null,
        is_winner bit not null,
        paid_amount decimal(8,2),
        split_price decimal(8,2) not null,
        id_booking bigint not null,
        id_player bigint not null,
        id_player_booking bigint not null auto_increment,
        paid_at datetime(6),
        payment_id varchar(255),
        payment_method enum ('CASH','CREDIT_CARD','ONLINE','WALLET'),
        team enum ('A','B','NONE') not null,
        primary key (id_player_booking)
    ) engine=InnoDB;

    create table player_reviews (
        score integer not null check ((score<=5) and (score>=1)),
        created_at datetime(6),
        id_player_review bigint not null auto_increment,
        id_rated_player bigint not null,
        id_rater bigint not null,
        comment varchar(255),
        primary key (id_player_review)
    ) engine=InnoDB;

    create table player_sport_positions (
        id_player_sport bigint not null,
        id_position bigint not null
    ) engine=InnoDB;

    create table player_sports (
        level float(53) not null,
        losses integer not null check ((losses>=0)),
        played_matches integer not null check ((played_matches>=0)),
        wins integer not null check ((wins>=0)),
        id_player bigint not null,
        id_player_sport bigint not null auto_increment,
        id_sport bigint not null,
        last_level_change datetime(6),
        dominant_side enum ('AMBIDEXTROUS','LEFT','NOT_APPLICABLE','RIGHT'),
        primary key (id_player_sport)
    ) engine=InnoDB;

    create table players (
        birth_date date,
        karma integer not null check ((karma<=100) and (karma>=0)),
        profile_complete bit not null,
        public_profile bit not null,
        id_city bigint,
        id_player bigint not null auto_increment,
        id_user bigint,
        phone varchar(20),
        name varchar(50),
        surname varchar(50),
        avatar_url varchar(255),
        biography varchar(255),
        default_payment_method_id varchar(255),
        stripe_customer_id varchar(255),
        gender enum ('FEMALE','MALE','OTHER'),
        primary key (id_player)
    ) engine=InnoDB;

    create table provinces (
        label varchar(255) not null,
        province_code varchar(255) not null,
        region_code varchar(255),
        primary key (province_code)
    ) engine=InnoDB;

    create table regions (
        label varchar(255) not null,
        region_code varchar(255) not null,
        primary key (region_code)
    ) engine=InnoDB;

    create table roles (
        id_role bigint not null auto_increment,
        name varchar(50) not null,
        primary key (id_role)
    ) engine=InnoDB;

    create table sport_positions (
        id_main_position bigint not null,
        id_sport bigint not null,
        id_sport_position bigint not null auto_increment,
        description varchar(50),
        name varchar(50) not null,
        primary key (id_sport_position)
    ) engine=InnoDB;

    create table sports (
        is_team_sport bit not null,
        players_per_match integer not null check ((players_per_match>=1)),
        players_per_team integer not null check ((players_per_team>=0)),
        color varchar(7),
        id_sport bigint not null auto_increment,
        name varchar(50) not null,
        icon_url varchar(255) not null,
        primary key (id_sport)
    ) engine=InnoDB;

    create table surfaces (
        id_surface bigint not null auto_increment,
        name varchar(50) not null,
        description varchar(100),
        icon_url varchar(255),
        primary key (id_surface)
    ) engine=InnoDB;

    create table user_authorities (
        id_authority bigint not null,
        id_user bigint not null,
        primary key (id_authority, id_user)
    ) engine=InnoDB;

    create table user_notification_prefs (
        enabled bit not null,
        id bigint not null auto_increment,
        id_user bigint not null,
        notification_type enum ('BOOKING_CANCELLED','BOOKING_CONFIRMED','FRIEND_ACCEPTED','FRIEND_REQUEST','JOIN_ACCEPTED','JOIN_REJECTED','JOIN_REQUEST','LEVEL_UP','MATCH_READY','NEW_MESSAGE','PARTICIPANT_JOINED','PARTICIPANT_LEFT','RESULT_PENDING','SYSTEM_ALERT') not null,
        primary key (id)
    ) engine=InnoDB;

    create table users (
        enabled bit not null,
        locked bit not null,
        creation_date datetime(6) not null,
        expiry_date datetime(6),
        id_role bigint,
        id_user bigint not null auto_increment,
        username varchar(50) not null,
        email varchar(100) not null,
        fcm_token varchar(512),
        password varchar(255) not null,
        primary key (id_user)
    ) engine=InnoDB;

    alter table activation_tokens 
       add constraint UK7gk0sgbu5kmofr1n7xei6k26o unique (id_user);

    alter table activation_tokens 
       add constraint UK5jny0xpou62bqdjhkbw1c0qxd unique (token);

    alter table authorities 
       add constraint UKnb3atvjf9ov5d0egnuk47o5e unique (name);

    alter table clubs 
       add constraint UKk8ri1ow2p79fqvsui7cylak45 unique (nif);

    alter table clubs 
       add constraint UKiovve4w9tgapllooqjhju80we unique (phone);

    alter table clubs 
       add constraint UKq5c0dvdxphxiksvc50hsh53v1 unique (contact_email);

    alter table friendships 
       add constraint UKjujkxdk8yymq29aupfs7g6etb unique (id_requester, id_recipient);

    alter table join_requests 
       add constraint UK4wg0p7nntehlo3p5yevyg3jls unique (id_booking, id_player);

    alter table organizations 
       add constraint UK46gspyum9s50xlg35l9tycurv unique (id_user);

    alter table organizations 
       add constraint UKjp9bf801x0g3g9q7dbdo3blen unique (cif);

    alter table organizations 
       add constraint UKm913d441k0pvuje2fuvgkyfhk unique (business_name);

    alter table player_bookings 
       add constraint UKhm94gx41qn5182avjkgd83lea unique (id_player, id_booking);

    alter table player_sports 
       add constraint UKos7piub52un3pqbmdtqkdne3u unique (id_player, id_sport);

    alter table players 
       add constraint UKa2lbekmluj7xjhfaprdoxvsiw unique (id_user);

    alter table players 
       add constraint UKp4ehqrxqot49ecrj90709botp unique (phone);

    alter table roles 
       add constraint UKofx66keruapi6vyqpv6f2or37 unique (name);

    alter table sports 
       add constraint UKtj61or3k005spbrx0lgjpwtde unique (name);

    alter table surfaces 
       add constraint UK7wj431rn8tcjj46jmhp7xkr5a unique (name);

    alter table user_notification_prefs 
       add constraint UK3vdq2nqineo1t2co1rrq6av5h unique (id_user, notification_type);

    alter table users 
       add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

    alter table users 
       add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

    alter table activation_tokens 
       add constraint FKddwuel6p38uhlh8f6tnyiv54q 
       foreign key (id_user) 
       references users (id_user);

    alter table bookings 
       add constraint FK9sj0his787vx27qad5bdw71pf 
       foreign key (id_court) 
       references courts (id_court);

    alter table bookings 
       add constraint FK7n19id7tbioal42r2i75bg4fx 
       foreign key (id_user) 
       references users (id_user);

    alter table cities 
       add constraint FKq1kpald6bqow9ob7rx9i8s6cg 
       foreign key (province_code) 
       references provinces (province_code);

    alter table club_balance_entries 
       add constraint FKp3sd6ihvp7t9gctcg9h0rp3uf 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table club_balance_entries 
       add constraint FKtcdff686nayflmxn4i913l5nk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKtq0sfw4yrwaa8m0chasctlvgk 
       foreign key (id_club) 
       references clubs (id_club);

    alter table club_reviews 
       add constraint FKl1jb7yy69jkjfqy8nv9dw5leh 
       foreign key (id_rater) 
       references players (id_player);

    alter table clubs 
       add constraint FK6ha6q1nc386680wfsk11oje9t 
       foreign key (id_city) 
       references cities (id_city);

    alter table clubs 
       add constraint FKnd9y0cqoyubm5mirbgyth751v 
       foreign key (id_organization) 
       references organizations (id_organization);

    alter table conversation_participants 
       add constraint FK6o66i3oqe1w2svngu7n7kbf2p 
       foreign key (id_user) 
       references users (id_user);

    alter table conversation_participants 
       add constraint FKs1pe3fkv8abeniidi8ouka3lc 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table court_blocks 
       add constraint FKejge1al02mtd3e9u5n3godyid 
       foreign key (id_court) 
       references courts (id_court);

    alter table court_schedules 
       add constraint FKo9plcchg8m9khprg6p7ty34du 
       foreign key (id_court) 
       references courts (id_court);

    alter table courts 
       add constraint FKd9g5muaccfvellut8vsix22tx 
       foreign key (id_club) 
       references clubs (id_club);

    alter table courts 
       add constraint FKdn4q2spas9l49sll0elp19r9u 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table courts 
       add constraint FKmlnu6o52oep2bjevvfd1ltkqo 
       foreign key (id_surface) 
       references surfaces (id_surface);

    alter table friendships 
       add constraint FKtc36fp7369o6pling9jv696pt 
       foreign key (id_recipient) 
       references users (id_user);

    alter table friendships 
       add constraint FKt1hbamgoeh8j6lwbp7j1ew6vs 
       foreign key (id_requester) 
       references users (id_user);

    alter table horaries_clubs 
       add constraint FKc8ygb50884b2cdjcrtnbxopb3 
       foreign key (id_club) 
       references clubs (id_club);

    alter table join_requests 
       add constraint FKetw4eryumf2w0vyoei5sricgc 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table join_requests 
       add constraint FKg0vc3di2oenv0afbq59kpgd8p 
       foreign key (id_player) 
       references players (id_player);

    alter table messages 
       add constraint FK15pncav1yft13vh4ir6qt6o4y 
       foreign key (id_conversation) 
       references conversations (id_conversation);

    alter table messages 
       add constraint FK3nwahn9plcioqsad6fbidro7m 
       foreign key (id_sender) 
       references users (id_user);

    alter table notifications 
       add constraint FKhrebgqe9mgp6x2erxipscbxpi 
       foreign key (id_user) 
       references users (id_user);

    alter table organizations 
       add constraint FKol09qh7vnablxl7yk9450u9lq 
       foreign key (id_city) 
       references cities (id_city);

    alter table organizations 
       add constraint FK31chp6t6n52q5fi15m7hskv89 
       foreign key (id_user) 
       references users (id_user);

    alter table player_bookings 
       add constraint FK1ucnm2ndkpbnmjngqxed75y43 
       foreign key (id_booking) 
       references bookings (id_booking);

    alter table player_bookings 
       add constraint FK558mxgubfiogg01enka6e1prs 
       foreign key (id_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FKgvmbthbpmk1hbpyuajls0b26m 
       foreign key (id_rated_player) 
       references players (id_player);

    alter table player_reviews 
       add constraint FK3t7777djyqsmv3ilxvwata59k 
       foreign key (id_rater) 
       references players (id_player);

    alter table player_sport_positions 
       add constraint FKbxcxspgy2tgesaq2n2wp4r4j9 
       foreign key (id_position) 
       references sport_positions (id_sport_position);

    alter table player_sport_positions 
       add constraint FKnpbx3got94pkk1uhl1lpiqyqr 
       foreign key (id_player_sport) 
       references player_sports (id_player_sport);

    alter table player_sports 
       add constraint FKb77t5mb4i16s4jgrh7n11m70k 
       foreign key (id_player) 
       references players (id_player);

    alter table player_sports 
       add constraint FKlqjgg7qepn7vsekd2l6mh8gpw 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table players 
       add constraint FKfs56gdu99u8fysp3i2h18s4cp 
       foreign key (id_city) 
       references cities (id_city);

    alter table players 
       add constraint FK851yg4yct646kgu95cq4glw2u 
       foreign key (id_user) 
       references users (id_user);

    alter table provinces 
       add constraint FKlnwh9ppis015see8jinynepxn 
       foreign key (region_code) 
       references regions (region_code);

    alter table sport_positions 
       add constraint FK1fvo33flyaqtsogd15km4bepj 
       foreign key (id_sport) 
       references sports (id_sport);

    alter table user_authorities 
       add constraint FKefqce5pe254q5hym7rgii9kuj 
       foreign key (id_authority) 
       references authorities (id_authority);

    alter table user_authorities 
       add constraint FKdh0uxx0dxjh34tgxrdvcv48c3 
       foreign key (id_user) 
       references users (id_user);

    alter table user_notification_prefs 
       add constraint FKolef4elu5g9bx9hwmloldifp2 
       foreign key (id_user) 
       references users (id_user);

    alter table users 
       add constraint FKt92dgi4412ywy3u8tm9jwdya5 
       foreign key (id_role) 
       references roles (id_role);
