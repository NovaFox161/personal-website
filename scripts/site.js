Y.use('node', 'event', function (Y) {

    var headerLayout = function () {
        /* Fix the layout if header is too tall. */
        var headerHeight = Y.one('#header').get('clientHeight');
        var announcementHeight = Y.one('.sqs-announcement-bar') ? Y.one('.sqs-announcement-bar').get('clientHeight') : 0;
        var sitePadding = parseInt(Y.one('#site').getStyle('paddingTop'));

        if (Y.one('body').get('clientWidth') > 640 || !Y.one('.mobile-nav')) {
            Y.one('#site').setStyle('marginTop', headerHeight + announcementHeight - sitePadding + 'px');
        } else {
            Y.one('#site').setStyle('marginTop', '');
        }
    };

    var takk = function () {

        headerLayout();

        /* Set height of content for tall sidebars */
        if (Y.one('#sidebar')) {
            var sidebarHeight = Y.one('#sidebar .sqs-layout').getComputedStyle('height');
            Y.one('#content > .wrapper').setStyle('minHeight', sidebarHeight);
        }

        /* Add spans to gallery controls */
        Y.all('.sqs-gallery-controls a').each(function (a) {
            var span = a.getHTML();
            a.setHTML('<i></i><span>' + span + '</span>').setStyle('display', 'block');
        });

        /* Set width of dropdown folders */
        if (Y.one('.folder-wrapper')) {
            Y.all('.primary-nav .folder-wrapper').each(function (fw) {
                fw.one('.folder').setStyle('minWidth', fw.getComputedStyle('width'));
            });
        }

        if (Y.one('.primary-nav .folder')) {
            Y.one('.primary-nav').all('.folder').each(function (f) {
                if (f.getX() + f.get('clientWidth') > Y.one('body').get('clientWidth')) {
                    f.setStyles({
                        'right': '0',
                        'margin-right': '0'
                    });
                }
            });
        }

        /* Set social icons */
        if (Y.one('.social-on')) {
            if (Y.one('body').get('clientWidth') > 640 || !Y.one('.mobile-nav')) {
                var footerWidth = parseInt(Y.one('#sqs-layout-footer').getComputedStyle('width'), 10);
                socialWidth = parseInt(Y.one('#sqs-social').getComputedStyle('width'), 10);

                Y.one('#sqs-layout-footer').setStyles({
                    'marginRight': socialWidth,
                    'maxWidth': '(footerWidth - socialWidth)'
                });
            }
        }

        /* Set mobile nav */
        if (Y.one('body.mobile-style-available')) {

            var mn = Y.one('.mobile-nav');
            var showNav = mn.one('.show-nav');

            showNav && showNav.on('click', function () {
                mn.toggleClass('show');
            });

            mn.all('.folder-wrapper').each(function (mf) {
                var showFolder = mf.one('.show-folder');
                showFolder && showFolder.on('click', function () {
                    mf.toggleClass('show');
                });
            });
        }

        // Announcement Bar fix - offsets header position to prevent covering nav
        var announcementBar = Y.one('.sqs-announcement-bar');

        if (announcementBar) {

            var offset = announcementBar.get('clientHeight');
            var announcementBarClose = Y.one('.sqs-announcement-bar-close');
            var header = Y.one('#header');
            var fixedHeader = Y.one('.fixed-header-position-on');
            var mobileNav = Y.one('.mobile-nav');

            if (fixedHeader) {
                announcementBar.setStyles({
                    'position': 'fixed',
                    'width': '100%'
                });
            }

            if (header && announcementBarClose) {

                header.setStyle('top', offset);

                announcementBarClose.on('click', function () {
                    header.setStyle('top', '0');
                });
            }

            if (mobileNav && mobileNav.getStyle('display') != 'none') {
                if (announcementBar.getStyle('position') == 'fixed') {
                    announcementBar.setStyle('position', 'relative');
                }

                Y.Global.on('windowresize', function () {
                    console.log('resizing');
                });

                mobileNav.setStyle('top', offset);
                announcementBarClose.on('click', function () {
                    mobileNav.setStyle('top', '0');
                });
            }

        }

    };

    Y.on('domready', takk);
    Y.on('resize', headerLayout);
    Y.Global.on('tweak:change', headerLayout);

});
