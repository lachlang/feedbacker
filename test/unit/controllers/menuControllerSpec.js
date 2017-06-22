'use strict';

describe('menu controller [MenuCtrl]', function() {

  var scope, menuController, model, session, $location, btnConfig;
  var deferred;

  beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Session_, _$location_, _uibButtonConfig_) {
    scope = $rootScope.$new();

    deferred = $q.defer();

    $location = _$location_;
    spyOn($location, 'path');

    model = _Model_;
    spyOn(model, 'getCurrentUser').and.returnValue(deferred.promise);
    spyOn(model, 'flush');

    btnConfig = _uibButtonConfig_;

    session = _Session_;
    spyOn(session, 'login').and.returnValue(deferred.promise);
    spyOn(session, 'logout').and.returnValue(deferred.promise);
    spyOn(session, 'validSession');

    menuController = $controller('MenuCtrl',{$scope: scope, uibButtonConfig: btnConfig });
  }));

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
      expect(angular.isFunction(menuController.logout)).toBe(true);
      expect(angular.isFunction(menuController.logout)).toBe(true);
      expect(angular.isFunction(menuController.initialiseAuthenticatedContent)).toBe(true);
      expect(angular.isFunction(menuController.isLoggedIn)).toBe(true);
      expect(angular.isFunction(menuController.isLoggedInLeader)).toBe(true);
      expect(angular.isFunction(menuController.isLoggedInAdmin)).toBe(true);
      expect(angular.isFunction(menuController.isActive)).toBe(true);
    });

    it('for global controller variables', function() {
      expect(menuController).toBeDefined();
      expect(menuController.errors).toBeUndefined();
      expect(menuController.isLeader).toBe(false);
      expect(menuController.isAdmin).toBe(false);
      expect(menuController.password).toBeUndefined();
      expect(menuController.username).toBeUndefined();
      expect(btnConfig.activeClass).toEqual('btn-primary');
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(model.getCurrentUser).not.toHaveBeenCalled();
    });

    it('calls initialises the scope for a valid session on page load', inject(function($controller) {
      session.validSession.and.returnValue(true);
      menuController = $controller('MenuCtrl',{$scope: scope });
      expect(model.getCurrentUser).toHaveBeenCalled();
    }));

  });

	describe('when logging in', function() {

		it('should call Session.login with the appropriate controller variables', function() {
			menuController.login("username", "password");
			expect(session.login).toHaveBeenCalledWith("username", "password");
		});

		it('should complete login actions on a successful request', function() {
		    spyOn(menuController, 'initialiseAuthenticatedContent');

			menuController.login("username", "password");

            deferred.resolve();
            scope.$digest();

			expect(menuController.initialiseAuthenticatedContent).toHaveBeenCalled();
			expect($location.path).toHaveBeenCalledWith("/worklist");
		});

		it('should call reset error messages', function() {
			menuController.error = "some value";

			menuController.login("a@b.co", 1);
			expect(menuController.error).toBeUndefined();
		});

		it('should reset the password controller field on completion', function() {
			menuController.password = "something";

            menuController.login();

			expect(model.password).toBeUndefined();
		});

		it('should call set and error message when unsuccessful', inject(function($rootScope) {
			spyOn($rootScope, '$broadcast');

			menuController.login("username", "password");

            var response = {response:{}}
			deferred.reject(response);
			scope.$digest();

            expect($rootScope.$broadcast).not.toHaveBeenCalled();
			expect(menuController.error).toEqual("Could not log in.  Please try again later.");

			menuController.login("username", "password");

            response.status = 401;
			deferred.reject(response);
			scope.$digest();

            expect($rootScope.$broadcast).toHaveBeenCalledWith('invalid-credentials', {});
			expect(menuController.error).toEqual("Could not log in.  Please try again later.");

			menuController.login("username", "password");

            response.status = 406;
			deferred.reject({response: {status: 406}});
			scope.$digest();

            expect($rootScope.$broadcast).toHaveBeenCalledWith('inactive-account', {});
			expect(menuController.error).toEqual("Could not log in.  Please try again later.");
		}));
	});

	describe('when logging out', function() {

		it('should call clean the session', function() {
            menuController.username = "username";
            menuController.password = "password";
            menuController.isLeader = true;
            menuController.isAdmin = true;
            menuController.error = "some error";

			menuController.logout();

            expect(menuController.username).toEqual("username");
            expect(menuController.password).toBeUndefined();
            expect(menuController.error).toBeUndefined();
            expect(menuController.isLeader).toBe(false);
            expect(menuController.isAdmin).toBe(false);

			expect(session.logout).toHaveBeenCalled();
			expect($location.path).toHaveBeenCalledWith("/landing");
			expect(model.flush).toHaveBeenCalled();
		});

    });

    describe('when authenticating the session', function() {

        it('should call  model.getCurrentUser', function() {
            menuController.initialiseAuthenticatedContent();

            expect(model.getCurrentUser).toHaveBeenCalled();
        });

        it('should call  model.getCurrentUser', function() {
            menuController.initialiseAuthenticatedContent();

            deferred.resolve({isLeader: true, isAdmin: true});
            scope.$digest();

            expect(menuController.error).toBeUndefined();
            expect(menuController.isLeader).toBe(true);
            expect(menuController.isAdmin).toBe(true);
        });

        it('should surpress errors', function() {
            menuController.initialiseAuthenticatedContent();

            deferred.reject({isLeader: true, isAdmin: true});
            scope.$digest();

            expect(menuController.error).toBeUndefined();
            expect(menuController.isLeader).toBe(false);
            expect(menuController.isAdmin).toBe(false);
        });
    });

    describe('should have basic helper functions to', function() {

        it('clear errors from the controller', function() {
            menuController.error = "something";

            menuController.resetError();

            expect(menuController.errors).toBeUndefined();
        });

		it('check the active location', function() {
		    $location.path.and.returnValue("address");

			expect(menuController.isActive("address")).toBe(true);
			expect(menuController.isActive("not address")).toBe(false);
		});

		it('check if the session is valid', function() {
		    session.validSession.and.returnValue(true);
			var result = menuController.isLoggedIn();

			expect(session.validSession).toHaveBeenCalled();
			expect(result).toBe(true);

            session.validSession.and.returnValue(false);
            result = menuController.isLoggedIn();
            expect(result).toBe(false);
		});

		it('check if the logged on user has direct reports', function() {

			spyOn(menuController, 'isLoggedIn').and.returnValue(false);
			menuController.isLeader = false;
			expect(menuController.isLoggedInLeader()).toBe(false);

			menuController.isLeader = true;
			expect(menuController.isLoggedInLeader()).toBe(false);

			menuController.isLoggedIn.and.returnValue(true);
			menuController.isLeader = false;
			expect(menuController.isLoggedInLeader()).toBe(false);

			menuController.isLeader = true;
			expect(menuController.isLoggedInLeader()).toBe(true);
		});

		it('check if the logged on user is an administrator', function() {

			spyOn(menuController, 'isLoggedIn').and.returnValue(false);
			menuController.isAdmin = false;
			expect(menuController.isLoggedInAdmin()).toBe(false);

			menuController.isAdmin = true;
			expect(menuController.isLoggedInAdmin()).toBe(false);

			menuController.isLoggedIn.and.returnValue(true);
			menuController.isAdmin = false;
			expect(menuController.isLoggedInAdmin()).toBe(false);

			menuController.isAdmin = true;
			expect(menuController.isLoggedInAdmin()).toBe(true);
		});
	});

});
