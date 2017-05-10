feedbackerApp.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.when('/about', {
			templateUrl: 'fragments/about.html',
			requireLogin: false
		});
		$routeProvider.when('/admin', {
			templateUrl: 'components/admin/admin.html',
			requireLogin: true
		});
		$routeProvider.when('/activationEmail', {
			templateUrl: 'components/activate/activate.html',
			requireLogin: false
		});
		$routeProvider.when('/detailEdit', {
			templateUrl: 'components/detail/edit.html',
			requireLogin: true
		});
		$routeProvider.when('/detailView', {
			templateUrl: 'components/detail/view.html',
			requireLogin: true
		});
		$routeProvider.when('/landing', {
			templateUrl: 'components/landing/landing.html',
			requireLogin: false
		});
		$routeProvider.when('/nominate', {
			templateUrl: 'components/nominate/nominate.html',
			requireLogin: false
		});
		$routeProvider.when('/profile', {
			templateUrl: 'components/profile/profile.html',
			requireLogin: false
		});
		$routeProvider.when('/provide', {
			templateUrl: 'components/provide/provide.html',
			requireLogin: false
		});
		$routeProvider.when('/resetPassword', {
			templateUrl: 'components/reset/resetPassword.html',
			requireLogin: false
		});
		$routeProvider.when('/resetPasswordEmail', {
			templateUrl: 'components/reset/resetPasswordEmail.html',
			requireLogin: false
		});
		$routeProvider.when('/reports', {
			templateUrl: 'components/reports/reports.html',
			requireLogin: true
		});
		$routeProvider.when('/signOut', {
			templateUrl: 'components/landing/landing.html',
			requireLogin: false
		});
		$routeProvider.when('/summary', {
			templateUrl: 'components/summary/summary.html',
			requireLogin: true
		});
		$routeProvider.when('/terms', {
			templateUrl: 'fragments/termsAndConditions.html',
			requireLogin: false
		});
		$routeProvider.otherwise({
			redirectTo: '/landing'
		});
		$routeProvider.when('/worklist', {
			templateUrl: 'components/list/list.html',
			requireLogin: true
		});
	}
]);