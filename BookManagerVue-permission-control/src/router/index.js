import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/register',
    component: () => import('@/views/register/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: { title: '首页', icon: 'dashboard' }
    }]
  }
]

/**
 * asyncRoutes
 * the routes that need to be dynamically loaded based on user roles
 */
export const asyncRoutes = [

  {
    path: '/bookmanage',
    name: 'Bookmanage',
    component: Layout,
    redirect: '/bookmanage/bookinfo',
    alwaysShow: true,
    meta: {
      title: '图书管理',
      icon: 'book'
    },
    children: [
      {
        path: 'bookinfo',
        name: 'Bookinfo',
        component: () => import('@/views/bookinfo/index'),
        meta: {
          title: '图书信息管理',
          icon: 'form',
          roles: ['admin', 'reader', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'booktype',
        name: 'Booktype',
        component: () => import('@/views/booktype/index'),
        meta: {
          title: '图书类型管理',
          icon: 'example',
          roles: ['admin', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'borrow',
        name: 'Borrow',
        component: () => import('@/views/borrow/index'),
        meta: {
          title: '借阅信息管理',
          icon: 'borrow',
          roles: ['admin', 'reader', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/inventory/index'),
        meta: {
          title: '库存管理',
          icon: 'table',
          roles: ['admin', 'super_admin'],
          noCache: true
        }
      }
    ]
  },

  {
    path: '/account',
    name: 'Account',
    component: Layout,
    redirect: '/account/profile',
    alwaysShow: true,
    meta: {
      title: '账号管理',
      icon: 'user'
    },
    children: [
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/account/index'),
        meta: {
          title: '我的账号',
          icon: 'user',
          roles: ['admin', 'reader', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'password',
        name: 'Password',
        component: () => import('@/views/password/index'),
        meta: {
          title: '修改密码',
          icon: 'password',
          roles: ['admin', 'reader', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/index'),
        meta: {
          title: '用户管理',
          icon: 'form',
          roles: ['admin', 'super_admin'],
          noCache: true
        }
      }
    ]
  },

  {
    path: '/message',
    name: 'MessageCenter',
    component: Layout,
    redirect: '/message/reminder',
    alwaysShow: true,
    meta: {
      title: '消息中心',
      icon: 'message',
      roles: ['reader']
    },
    children: [
      {
        path: 'reminder',
        name: 'Reminder',
        component: () => import('@/views/message/reminder'),
        meta: {
          title: '催还信息',
          icon: 'bell',
          noCache: true
        }
      }
    ]
  },

  {
    path: '/system',
    name: 'System',
    component: Layout,
    redirect: '/system/logs',
    alwaysShow: true,
    meta: {
      title: '系统管理',
      icon: 'form',
      roles: ['admin', 'super_admin']
    },
    children: [
      {
        path: 'logs',
        name: 'OperationLogs',
        component: () => import('@/views/system/logs'),
        meta: {
          title: '操作日志',
          icon: 'document',
          roles: ['admin', 'super_admin'],
          noCache: true
        }
      },
      {
        path: 'settings',
        name: 'SystemSettings',
        component: () => import('@/views/system/settings'),
        meta: {
          title: '系统设置',
          icon: 'setting',
          roles: ['super_admin'],
          noCache: true
        }
      }
    ]
  },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
