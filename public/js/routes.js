feedbackerApp.config(['$routeProvider',
	function($routeProvider) {
		$routeProvider.when('/about', {
			templateUrl: 'fragments/about.html',
			requireLogin: false
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
		$routeProvider.when('/list', {
			templateUrl: 'components/list/list.html',
			requireLogin: true
		});
		$routeProvider.when('/nominate', {
			templateUrl: 'components/nominate/nominate.html',
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
		$routeProvider.when('/terms', {
			templateUrl: 'fragments/termsAndConditions.html',
			requireLogin: false
		});
		$routeProvider.otherwise({
			redirectTo: '/landing'
		});
	}
]);