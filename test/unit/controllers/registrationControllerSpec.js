'use strict';

describe('provide feedback controller [RegistrationCtrl]', function() {

  var scope, rootScope, registrationController, account, deferred;

  beforeEach(module('feedbacker'));

  // define the mock person and relationship services
  beforeEach(inject(function($rootScope, $q, $controller, _Account_) {
    scope = $rootScope.$new();
    rootScope = $rootScope;
    spyOn(rootScope, '$on');

    deferred = $q.defer();

    account = _Account_;
    spyOn(account, 'register').and.returnValue(deferred.promise);

    registrationController = $controller('RegistrationCtrl',{$scope: scope });
  }));

  describe('has valid initialisation values', function() {

    it('should define functions', function() {
      expect(angular.isFunction(registrationController.register)).toBe(true);
    });

    it('for global controller variables', function() {
      expect(registrationController).toBeDefined();
      expect(registrationController.error).toBeUndefined();
      expect(registrationController.message).toBeUndefined();
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(rootScope.$on).toHaveBeenCalled();
      expect(rootScope.$on.calls.count()).toBe(2);
    });

  });

  describe('when submitting feedback', function() {

    it('should reset the display messages in the controller', function() {
      registrationController.message = "message";
      registrationController.error = "error";
      registrationController.register();
      expect(registrationController.message).toBeUndefined();
      expect(registrationController.error).toBeUndefined();
      expect(registrationController.update).toEqual("We are processing your registration...");
    });

    it('should call the account.register service and update the controller values', function() {
      registrationController.name = "name";
      registrationController.role = "role";
      registrationController.email = "email";
      registrationController.password = "password";
      registrationController.managerEmail = "managerEmail";
      registrationController.register();
      expect(account.register).toHaveBeenCalledWith("name", "role", "email", "password", "managerEmail");

      deferred.resolve();
      scope.$digest();

      expect(registrationController.name).toBeUndefined();
      expect(registrationController.role).toBeUndefined();
      expect(registrationController.email).toBeUndefined();
      expect(registrationController.password).toBeUndefined();
      expect(registrationController.managerEmail).toBeUndefined();
      expect(registrationController.update).toBeUndefined();
      expect(registrationController.message).toEqual("Thankyou for registering.  An activation email has been sent to your email address.");
    });

    it('should set the error message when the account.register fails', function() {
      registrationController.register();
      deferred.reject({"data":{"message": "error message"}});
      scope.$digest();

      expect(registrationController.update).toBeUndefined();
      expect(registrationController.error).toEqual("error message");
    });
	});
});
