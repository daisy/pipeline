module.exports = {
    env: {
        es2020: true,
        node: true,
    },
    extends: ['prettier', 'plugin:prettier/recommended'],
    parser: '@typescript-eslint/parser',
    parserOptions: {
        ecmaVersion: 2020,
        sourceType: 'module',
    },
    plugins: ['@typescript-eslint', 'prettier'],
    rules: {
        '@typescript-eslint/no-empty-interface': 0,
        'prettier/prettier': [
            'error',
            {
                semi: false,
                endOfLine: 'auto',
                singleQuote: true,
                tabWidth: 4,
                useTabs: false,
                trailingComma: 'es5',
            },
        ],
    },
}
