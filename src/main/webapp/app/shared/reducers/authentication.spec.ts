import axios from 'axios';
import sinon from 'sinon';
import { Storage } from 'react-jhipster';
import { configureStore, createReducer } from '@reduxjs/toolkit';

import authentication, {
  authError,
  authenticate,
  clearAuth,
  clearAuthToken,
  clearAuthentication,
  getAccount,
  initialState,
  login,
  logout,
  logoutSession,
} from 'app/shared/reducers/authentication';

describe('Authentication reducer tests', () => {
  function isAccountEmpty(state): boolean {
    return Object.keys(state.account).length === 0;
  }

  describe('Common tests', () => {
    it('should return the initial state', () => {
      const toTest = authentication(undefined, { type: '' });
      expect(toTest).toMatchObject({
        loading: false,
        isAuthenticated: false,
        errorMessage: null, // Errors returned from server side
        loginSuccess: false,
        loginError: false, // Errors returned from server side
        showModalLogin: false,
        redirectMessage: null,
      });
      expect(isAccountEmpty(toTest));
    });
  });

  describe('Requests', () => {
    it('should detect a request', () => {
      expect(authentication(undefined, { type: authenticate.pending.type })).toMatchObject({
        loading: true,
      });
      expect(authentication(undefined, { type: getAccount.pending.type })).toMatchObject({
        loading: true,
      });
    });
  });

  describe('Success', () => {
    it('should detect a success on login', () => {
      const toTest = authentication(undefined, { type: authenticate.fulfilled.type });
      expect(toTest).toMatchObject({
        loading: false,
        loginError: false,
        loginSuccess: true,
        showModalLogin: false,
      });
    });

    it('should detect a success on get session and be authenticated', () => {
      const payload = { data: { activated: true } };
      const toTest = authentication(undefined, { type: getAccount.fulfilled.type, payload });
      expect(toTest).toMatchObject({
        isAuthenticated: true,
        loading: false,
        account: payload.data,
      });
    });

    it('should detect a success on get session and not be authenticated', () => {
      const payload = { data: { activated: false } };
      const toTest = authentication(undefined, { type: getAccount.fulfilled.type, payload });
      expect(toTest).toMatchObject({
        isAuthenticated: false,
        loading: false,
        account: payload.data,
      });
    });
  });

  describe('Failure', () => {
    it('should detect a failure on login', () => {
      const error = { message: 'Something happened.' };
      const toTest = authentication(undefined, { type: authenticate.rejected.type, error });

      expect(toTest).toMatchObject({
        errorMessage: error.message,
        showModalLogin: true,
        loginError: true,
      });
      expect(isAccountEmpty(toTest));
    });

    it('should detect a failure', () => {
      const error = { message: 'Something happened.' };
      const toTest = authentication(undefined, { type: getAccount.rejected.type, error });

      expect(toTest).toMatchObject({
        loading: false,
        isAuthenticated: false,
        showModalLogin: true,
        errorMessage: error.message,
      });
      expect(isAccountEmpty(toTest));
    });
  });

  describe('Other cases', () => {
    it('should properly reset the current state when a logout is requested', () => {
      const toTest = authentication(undefined, logoutSession());
      expect(toTest).toMatchObject({
        loading: false,
        isAuthenticated: false,
        loginSuccess: false,
        loginError: false,
        showModalLogin: true,
        errorMessage: null,
        redirectMessage: null,
      });
      expect(isAccountEmpty(toTest));
    });

    it('should properly define an error message and change the current state to display the login modal', () => {
      const message = 'redirect me please';
      const toTest = authentication(undefined, authError(message));
      expect(toTest).toMatchObject({
        loading: false,
        isAuthenticated: false,
        loginSuccess: false,
        loginError: false,
        showModalLogin: true,
        errorMessage: null,
        redirectMessage: message,
      });
      expect(isAccountEmpty(toTest));
    });

    it('should clear authentication', () => {
      const toTest = authentication({ ...initialState, isAuthenticated: true }, clearAuth());
      expect(toTest).toMatchObject({
        loading: false,
        showModalLogin: true,
        isAuthenticated: false,
      });
    });
  });

  describe('Actions', () => {
    let store;

    const resolvedObject = { value: 'whatever' };
    const getState = jest.fn();
    const dispatch = jest.fn();
    const extra = {};
    beforeEach(() => {
      store = configureStore({
        reducer: (state = [], action) => [...state, action],
      });
      axios.get = sinon.stub().returns(Promise.resolve(resolvedObject));
    });

    it('dispatches GET_SESSION_PENDING and GET_SESSION_FULFILLED actions', async () => {
      const result = await getAccount()(dispatch, getState, extra);

      const pendingAction = dispatch.mock.calls[0][0];
      expect(pendingAction.meta.requestStatus).toBe('pending');
      expect(getAccount.fulfilled.match(result)).toBe(true);
    });

    it('dispatches LOGOUT actions', async () => {
      await store.dispatch(logout());
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(logoutSession())]);
    });

    it('dispatches CLEAR_AUTH actions', async () => {
      await store.dispatch(clearAuthentication('message'));
      expect(store.getState()).toEqual([expect.any(Object), expect.objectContaining(authError('message')), clearAuth()]);
    });

    it('dispatches LOGIN, GET_SESSION and SET_LOCALE success and request actions', async () => {
      const loginResponse = { headers: { authorization: 'auth' } };
      axios.post = sinon.stub().returns(Promise.resolve(loginResponse));

      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const result = await authenticate('test')(dispatch, getState, extra);

      const pendingAction = dispatch.mock.calls[0][0];
      expect(pendingAction.meta.requestStatus).toBe('pending');
      expect(authenticate.fulfilled.match(result)).toBe(true);
    });
  });
  describe('clearAuthToken', () => {
    let store;
    const reducer = createReducer({ authentication: { account: { langKey: 'en' } } }, builder => {
      builder.addDefaultCase(() => {});
    });
    beforeEach(() => {
      store = configureStore({
        reducer,
      });
    });
    it('clears the session token on clearAuthToken', async () => {
      const AUTH_TOKEN_KEY = 'jhi-authenticationToken';
      const loginResponse = { headers: { authorization: 'Bearer TestToken' } };
      axios.post = sinon.stub().returns(Promise.resolve(loginResponse));

      await store.dispatch(login('test', 'test'));
      expect(Storage.session.get(AUTH_TOKEN_KEY)).toBe('TestToken');
      expect(Storage.local.get(AUTH_TOKEN_KEY)).toBe(undefined);
      clearAuthToken();
      expect(Storage.session.get(AUTH_TOKEN_KEY)).toBe(undefined);
      expect(Storage.local.get(AUTH_TOKEN_KEY)).toBe(undefined);
    });
    it('clears the local storage token on clearAuthToken', async () => {
      const AUTH_TOKEN_KEY = 'jhi-authenticationToken';
      const loginResponse = { headers: { authorization: 'Bearer TestToken' } };
      axios.post = sinon.stub().returns(Promise.resolve(loginResponse));

      await store.dispatch(login('user', 'user', true));
      expect(Storage.session.get(AUTH_TOKEN_KEY)).toBe(undefined);
      expect(Storage.local.get(AUTH_TOKEN_KEY)).toBe('TestToken');
      clearAuthToken();
      expect(Storage.session.get(AUTH_TOKEN_KEY)).toBe(undefined);
      expect(Storage.local.get(AUTH_TOKEN_KEY)).toBe(undefined);
    });
  });
});
